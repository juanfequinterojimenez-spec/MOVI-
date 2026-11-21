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
import com.example.MOVI.Exception.ResourceNotFoundException;
import com.example.MOVI.Repository.tarjetaRepository;
import com.example.MOVI.Repository.usuarioRepository;
import com.example.MOVI.Repository.viajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class viajeServiceIMP implements viajeService {
    private final viajeRepository viajeRepository;
    private final usuarioRepository usuarioRepository;
    private final tarjetaRepository tarjetaRepository;

    private static final int MAX_VIAJES = 20;
    private static final int PUNTOS_VIAJE = 25;
    private static final int ESPERA_MINUTOS = 15;
    private static final int VENTANA_TRANSBORDO = 110;

    @Override
    @Transactional
    public viajeResponseDTO registrarViaje(viajeSimulacionDTO request) {
        if (request == null || request.idUsuario() == null || request.idTarjeta() == null
                || request.nombreEstacion() == null || request.nombreEstacion().isBlank()
                || request.tipoComponente() == null || request.tipoComponente().isBlank()) {
            throw new BusinessRuleException("Completa todos los datos del viaje.");
        }

        String nuevoTipo = request.tipoComponente().trim().toUpperCase(Locale.ROOT);
        if (!nuevoTipo.equals("TRONCAL") && !nuevoTipo.equals("ZONAL")) {
            throw new BusinessRuleException("Elige Troncal o Zonal.");
        }

        Usuario usuario = usuarioRepository.findById(request.idUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Tarjeta tarjeta = tarjetaRepository.findById(request.idTarjeta())
                .orElseThrow(() -> new ResourceNotFoundException("Tarjeta no encontrada"));

        if (tarjeta.getUsuario() == null || !tarjeta.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessRuleException("La tarjeta no pertenece al usuario autenticado.");
        }
        if (tarjeta.getEstado() != estadoTarjeta.ACTIVA) {
            throw new BusinessRuleException("Necesitas una tarjeta activa para viajar.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = LocalDate.now().atTime(LocalTime.MAX);
        long viajesHoy = viajeRepository.countByUsuario_IdAndFechaHoraViajeBetween(
                usuario.getId(), inicioDia, finDia);
        if (viajesHoy >= MAX_VIAJES) {
            throw new BusinessRuleException("Alcanzaste el máximo de 20 viajes diarios.");
        }

        boolean esTransbordo = false;
        int puntosAAsignar = PUNTOS_VIAJE;
        Optional<Viaje> ultimoOpt = viajeRepository
                .findFirstByUsuario_IdOrderByFechaHoraViajeDesc(usuario.getId());

        if (ultimoOpt.isPresent()) {
            Viaje ultimo = ultimoOpt.get();
            LocalDateTime fechaUltimoViaje = ultimo.getFechaHoraViaje();
            if (fechaUltimoViaje != null) {
                long minutos = Duration.between(fechaUltimoViaje, ahora).toMinutes();
                String ultimoTipo = ultimo.getTipoComponente() == null ? "" :
                        ultimo.getTipoComponente().trim().toUpperCase(Locale.ROOT);
                boolean ventanaTransbordo = minutos >= 0 && minutos <= VENTANA_TRANSBORDO;
                boolean combinacionIntegrada = !("TRONCAL".equals(ultimoTipo) && "TRONCAL".equals(nuevoTipo));
                if (ventanaTransbordo && combinacionIntegrada) {
                    esTransbordo = true;
                    puntosAAsignar = 0;
                } else if (minutos >= 0 && minutos < ESPERA_MINUTOS) {
                    // Un viaje independiente antes de los 15 minutos queda bloqueado.
                    throw new BusinessRuleException("Espera 15 minutos para otro viaje independiente.");
                }
            }
        }

        Viaje nuevo = Viaje.builder()
                .usuario(usuario)
                .tarjeta(tarjeta)
                .nombreEstacion(request.nombreEstacion().trim())
                .tipoComponente(nuevoTipo)
                .puntosGenerados(puntosAAsignar)
                .build();

        int saldoAnterior = usuario.getPuntos() == null ? 0 : usuario.getPuntos();
        if (!esTransbordo) {
            usuario.setPuntos(saldoAnterior + puntosAAsignar);
            usuarioRepository.save(usuario);
        }

        Viaje guardado = viajeRepository.save(nuevo);
        return viajeResponseDTO.desde(guardado, saldoAnterior + puntosAAsignar, esTransbordo);
    }

    @Override
    @Transactional(readOnly = true)
    public Viaje obtenerPorId(Long id) {
        return viajeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Viaje no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViajeHistorialResponseDTO> listarHistorialPorUsuario(Long usuarioId) {
        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return viajeRepository.findByUsuario_IdOrderByFechaHoraViajeDesc(usuarioId)
                .stream().map(ViajeHistorialResponseDTO::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViajeHistorialResponseDTO> listarHistorialPorCorreo(String correoPasajero) {
        Usuario usuario = usuarioRepository.findByEmail(correoPasajero)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return listarHistorialPorUsuario(usuario.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public ViajeResumenResponseDTO obtenerResumenDiario(String correoPasajero) {
        Usuario usuario = usuarioRepository.findByEmail(correoPasajero)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = LocalDate.now().atTime(LocalTime.MAX);
        long viajesHoy = viajeRepository.countByUsuario_IdAndFechaHoraViajeBetween(
                usuario.getId(), inicioDia, finDia);
        return new ViajeResumenResponseDTO(
                usuario.getId(), usuario.getPuntos() == null ? 0 : usuario.getPuntos(),
                viajesHoy, Math.max(0, MAX_VIAJES - viajesHoy), MAX_VIAJES);
    }
}
