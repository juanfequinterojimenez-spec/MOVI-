package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.DTO.ResponseDTO.ViajeHistorialResponseDTO;
import com.example.MOVI.DTO.ResponseDTO.ViajeResumenResponseDTO;
import com.example.MOVI.DTO.ResponseDTO.viajeResponseDTO;
import com.example.MOVI.DTO.ResponseDTO.viajeSimulacionDTO;
import com.example.MOVI.Entity.Tarjeta;
import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Entity.Viaje;
import com.example.MOVI.Enum.estadoTarjeta;
import com.example.MOVI.Exception.BusinessRuleException;
import com.example.MOVI.Exception.CooldownExcepcion;
import com.example.MOVI.Exception.ForbiddenOperationException;
import com.example.MOVI.Exception.ResourceNotFoundException;
import com.example.MOVI.Repository.tarjetaRepository;
import com.example.MOVI.Repository.usuarioRepository;
import com.example.MOVI.Repository.viajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class viajeServiceIMP implements viajeService {

    private final viajeRepository viajeRepository;
    private final usuarioRepository usuarioRepository;
    private final tarjetaRepository tarjetaRepository;

    // limite de viajes
    private static final int maxViajes = 20;
    private static final int puntosViaje = 25;
    private static final int esperaViaje = 15;
    private static final int  transbordo = 110;

    // Registrar un viaje
    @Override
    @Transactional
    public viajeResponseDTO registrarViaje(viajeSimulacionDTO request) {

        Usuario usuario = usuarioRepository.findById(request.idUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + request.idUsuario()));

        Tarjeta tarjeta = tarjetaRepository.findById(request.idTarjeta())
                .orElseThrow(() -> new ResourceNotFoundException("Tarjeta TuLlave no encontrada con ID: " + request.idTarjeta()));


        if (!tarjeta.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessRuleException("Acceso denegado: La tarjeta no pertenece a este usuario.");
        }


        if (tarjeta.getEstado() != estadoTarjeta.ACTIVA) {
            throw new BusinessRuleException("No puedes realizar viajes. Tu tarjeta se encuentra " + tarjeta.getEstado().name().toLowerCase() + ".");
        }

        // 3. A partir de aquí continúan tus validaciones de transbordo, cooldown y límites diarios...
        Optional<Viaje> ultimoViajeOpt = viajeRepository.findFirstByUsuario_IdOrderByFechaHoraViajeDesc(usuario.getId());


        boolean esTransbordo = false;
        int puntosAAsignar = puntosViaje; // Por defecto 25 puntos

        if (ultimoViajeOpt.isPresent()) {
            // CORRECCIÓN 1: Extraemos correctamente el objeto Viaje de la variable Optional
            Viaje ultimoViaje = ultimoViajeOpt.get();
            LocalDateTime fechaUltimoViaje = ultimoViaje.getFechaHoraViaje();
            long minutosDesdeUltimoViaje = java.time.Duration.between(fechaUltimoViaje, LocalDateTime.now()).toMinutes();

            if (minutosDesdeUltimoViaje <= transbordo) {

                String ultimoTipo = ultimoViaje.getTipoComponente();
                String nuevoTipo = request.tipoComponente().toUpperCase();

                // Regla de integración de Bogotá (Troncal <-> Zonal o Zonal <-> Zonal)
                if (!("TRONCAL".equals(ultimoTipo) && "TRONCAL".equals(nuevoTipo))) {
                    esTransbordo = true;
                    puntosAAsignar = 0; // Transbordo integrado no otorga puntos
                }
            } else {
                // Si ya pasó la ventana de transbordo, aplicamos validación de cooldown de 15 min
                LocalDateTime tiempoPermitidoCooldown = fechaUltimoViaje.plusMinutes(esperaViaje);
                if (LocalDateTime.now().isBefore(tiempoPermitidoCooldown)) {
                    throw new BusinessRuleException("Debes esperar entre viajes independientes o verificar tu integración de transbordo.");
                }
            }
        }

        // limite de viajes diarios
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay(); // 00:00:00 de hoy
        LocalDateTime finDia = LocalDate.now().atTime(LocalTime.MAX); // 23:59:59 de hoy

        long viajesRealizadosHoy = viajeRepository.countByUsuario_IdAndFechaHoraViajeBetween(
                usuario.getId(),
                inicioDia,
                finDia
        );

        if (viajesRealizadosHoy >= maxViajes) {
            throw new BusinessRuleException("Límite diario alcanzado: Has superado el máximo de " + maxViajes + " viajes puntuables por día.");
        }

        // 3. Construir y guardar el viaje
        Viaje nuevoViaje = Viaje.builder()
                .usuario(usuario)
                .tarjeta(tarjeta)
                .nombreEstacion(request.nombreEstacion().trim())
                .tipoComponente(request.tipoComponente().trim())
                .puntosGenerados(puntosAAsignar) // 0 si es transbordo, 25 si es pasaje normal
                .build();

        // Solo sumamos puntos si NO es transbordo
        if (!esTransbordo) {
            usuario.setPuntos(usuario.getPuntos() + puntosAAsignar);
            usuarioRepository.save(usuario);
        }

        Viaje viajeGuardado = viajeRepository.save(nuevoViaje);

        return viajeResponseDTO.desde(viajeGuardado, usuario.getPuntos(), esTransbordo);
    }

    // Obtener viaje por ID
    @Override
    @Transactional(readOnly = true)
    public Viaje obtenerPorId(Long id) {

        return viajeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Viaje no encontrado con id " + id
                ));
    }

    // Listar historial de viajes por usuario id
    @Override
    @Transactional(readOnly = true)
    public List<ViajeHistorialResponseDTO> listarHistorialPorUsuario(Long usuarioId) {
        // 1. Verificar que el usuario exista
        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        // 2. Obtener los viajes ordenados del más reciente al más antiguo
        List<Viaje> viajes = viajeRepository.findByUsuario_IdOrderByFechaHoraViajeDesc(usuarioId);

        // 3. Mapear cada entidad a su respectivo DTO de historial
        return viajes.stream()
                .map(ViajeHistorialResponseDTO::desde)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViajeHistorialResponseDTO> listarHistorialPorCorreo(String correoPasajero) {

        Usuario usuario = usuarioRepository.findByEmail(correoPasajero)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correoPasajero));
        return listarHistorialPorUsuario(usuario.getId());}

    @Override
    @Transactional(readOnly = true)
    public ViajeResumenResponseDTO obtenerResumenDiario(String correoPasajero) {
        // 1. Buscar al pasajero por su correo
        Usuario usuario = usuarioRepository.findByEmail(correoPasajero)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correoPasajero));

        // 2. Contar cuántos viajes ha realizado hoy
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = LocalDate.now().atTime(LocalTime.MAX);

        long viajesHoy = viajeRepository.countByUsuario_IdAndFechaHoraViajeBetween(
                usuario.getId(),
                inicioDia,
                finDia
        );

        // 3. Calcular los viajes restantes (agregando el operador de resta faltante)
        long viajesRestantes = Math.max(0, maxViajes - viajesHoy);

        // 4. Retornar el DTO con el resumen
        return new ViajeResumenResponseDTO(
                usuario.getId(),
                usuario.getPuntos(),
                viajesHoy,
                viajesRestantes,
                maxViajes
        );
    }

}
