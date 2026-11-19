package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.DTO.ResponseDTO.tarjetaDTO;
import com.example.MOVI.DTO.ResponseDTO.tarjetaResponseDTO;
import com.example.MOVI.Entity.Tarjeta;
import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Enum.estadoTarjeta;
import com.example.MOVI.Exception.BusinessRuleException;
import com.example.MOVI.Exception.DuplicateResourceException;
import com.example.MOVI.Exception.ResourceNotFoundException;
import com.example.MOVI.Repository.tarjetaRepository;
import com.example.MOVI.Repository.usuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class tarjetaServiceIMP implements  tarjetaService {

    private final tarjetaRepository tarjetaRepository;
    private final usuarioRepository usuarioRepository;
    private final SupabaseStorageService supabaseStorageService;


    @Override
    public tarjetaResponseDTO vincular(String correoPasajero, tarjetaDTO dto) {
        // 1. Buscar al pasajero por su correo
        Usuario usuario = usuarioRepository.findByEmail(correoPasajero)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correoPasajero));

        // 2. Validar si el nuevo número de serie ya está registrado por otra persona
        if (tarjetaRepository.existsByNumeroSerie(dto.numeroSerie())) {
            throw new BusinessRuleException("El número de serie de esta tarjeta ya se encuentra registrado en el sistema.");
        }

        // 3. Buscar si el usuario ya tiene una tarjeta registrada (activa o bloqueada)
        Tarjeta tarjetaExistente = tarjetaRepository.findByUsuarioId(usuario.getId()).orElse(null);

        if (tarjetaExistente != null) {
            // Si ya tenía una tarjeta, la actualizamos con el NUEVO serial y la reactivamos
            tarjetaExistente.setNumeroSerie(dto.numeroSerie().trim());
            tarjetaExistente.setEstado(estadoTarjeta.ACTIVA);
            tarjetaExistente.setFechaVinculacion(LocalDateTime.now());

            Tarjeta tarjetaActualizada = tarjetaRepository.save(tarjetaExistente);
            return tarjetaResponseDTO.desde(tarjetaActualizada);
        } else {
            // Si es su primera vez vinculando una tarjeta, creamos el registro nuevo
            Tarjeta nuevaTarjeta = Tarjeta.builder()
                    .numeroSerie(dto.numeroSerie().trim())
                    .usuario(usuario)
                    .estado(estadoTarjeta.ACTIVA)
                    .fechaVinculacion(LocalDateTime.now())
                    .build();

            Tarjeta tarjetaGuardada = tarjetaRepository.save(nuevaTarjeta);
            return tarjetaResponseDTO.desde(tarjetaGuardada);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Tarjeta obtenerPorId(Long id) {

        return tarjetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tarjeta no encontrada con id " + id
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Tarjeta obtenerPorUsuario(Long usuarioId) {

        return tarjetaRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario " + usuarioId + " no tiene una tarjeta vinculada"
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tarjeta> listar() {
        return tarjetaRepository.findAll();
    }

    @Override
    @Transactional
    public Tarjeta cambiarEstado(Long id, estadoTarjeta nuevoEstado) {

        Tarjeta tarjeta = obtenerPorId(id);

        if (nuevoEstado == null) {
            throw new BusinessRuleException(
                    "El nuevo estado de la tarjeta es obligatorio"
            );
        }

        tarjeta.setEstado(nuevoEstado);

        return tarjetaRepository.save(tarjeta);
    }

    @Override
    @Transactional(readOnly = true)
    public Tarjeta obtenerPorUsuarioYEstado(Long usuarioId, estadoTarjeta estado) {

        if (estado == null) {
            throw new BusinessRuleException(
                    "El estado de la tarjeta es obligatorio"
            );
        }

        return tarjetaRepository.findByUsuario_IdAndEstado(usuarioId, estado)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario " + usuarioId +
                                " no tiene una tarjeta con estado " + estado
                ));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {

        Tarjeta tarjeta = obtenerPorId(id);

        tarjetaRepository.delete(tarjeta);
    }

    @Override
    public tarjetaResponseDTO registrarTarjetaConFoto(tarjetaDTO dto, MultipartFile fotoEvidencia) {
        Usuario usuario = usuarioRepository.findById(dto.idUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + dto.idUsuario()));

        // 2. Aplicar regla: una sola tarjeta ACTIVA por usuario
        tarjetaRepository.findByUsuario_IdAndEstado(usuario.getId(), estadoTarjeta.ACTIVA)
                .ifPresent(t -> {
                    throw new BusinessRuleException("El usuario ya cuenta con una tarjeta de transporte activa vinculada.");
                });

        // 3. Verificar si el número de serie ya está registrado
        if (tarjetaRepository.existsByNumeroSerie(dto.numeroSerie())) {
            throw new DuplicateResourceException("El número de serie de la tarjeta ya se encuentra registrado en el sistema.");
        }

        // 4. Validar formato de 16 dígitos
        if (!dto.numeroSerie().matches("^[0-9]{16}$")) {
            throw new BusinessRuleException("El serial de la tarjeta TuLlave debe tener exactamente 16 dígitos numéricos.");
        }

        // 5. Subir la foto de evidencia a Supabase Storage (carpeta "tarjetas-validadas")
        String urlEvidencia = null;
        if (fotoEvidencia != null && !fotoEvidencia.isEmpty()) {
            urlEvidencia = supabaseStorageService.subirArchivo(fotoEvidencia, "tarjetas-validadas");
        }

        // 6. Crear y guardar la nueva tarjeta vinculada
        Tarjeta nuevaTarjeta = Tarjeta.builder()
                .usuario(usuario)
                .numeroSerie(dto.numeroSerie())
                .estado(estadoTarjeta.ACTIVA)
                .build();

        Tarjeta tarjetaGuardada = tarjetaRepository.save(nuevaTarjeta);

        return new tarjetaResponseDTO(
                tarjetaGuardada.getIdTarjeta(),
                tarjetaGuardada.getNumeroSerie(),
                tarjetaGuardada.getEstado().name(),
                tarjetaGuardada.getFechaVinculacion()
        );
    }

    @Override
    public void bloquearTarjetaActual(String correoPasajero) {
        Usuario usuario = usuarioRepository.findByEmail(correoPasajero)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Buscamos la tarjeta que esté ACTIVA para este usuario
        Tarjeta tarjetaActiva = tarjetaRepository.findByUsuario_IdAndEstado(usuario.getId(), estadoTarjeta.ACTIVA)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes ninguna tarjeta activa para bloquear."));

        // Cambiamos el estado a bloqueada/inactiva (borrado lógico)
        tarjetaActiva.setEstado(estadoTarjeta.BLOQUEADA); // O INACTIVA segun tu enum
        tarjetaRepository.save(tarjetaActiva);
    }

    @Override
    @Transactional(readOnly = true)
    public tarjetaResponseDTO obtenerTarjetaActiva(String correoPasajero) {
        Usuario usuario = usuarioRepository.findByEmail(correoPasajero)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Buscamos la tarjeta ACTIVA asociada al pasajero
        Tarjeta tarjetaActiva = tarjetaRepository.findByUsuario_IdAndEstado(usuario.getId(), estadoTarjeta.ACTIVA)
                .orElseThrow(() -> new ResourceNotFoundException("No tienes ninguna tarjeta activa registrada actualmente."));

        return tarjetaResponseDTO.desde(tarjetaActiva);
    }

}


