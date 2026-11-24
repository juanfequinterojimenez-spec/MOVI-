
package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.DTO.ResponseDTO.tarjetaDTO;
import com.example.MOVI.DTO.ResponseDTO.tarjetaResponseDTO;
import com.example.MOVI.Entity.Tarjeta;
import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Enum.EstadoVerificacionTarjeta;
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
public class tarjetaServiceIMP implements tarjetaService {

    private final tarjetaRepository tarjetaRepository;
    private final usuarioRepository usuarioRepository;
    private final SupabaseStorageService supabaseStorageService;

    // =============================================
    // 1. VINCULAR SIN FOTO
    // =============================================

    @Override
    @Transactional
    public tarjetaResponseDTO vincular(
            String correoPasajero,
            tarjetaDTO dto
    ) {
        // Esta ruta registra una solicitud sin evidencia.
        // No verifica oficialmente la personalización.
        return guardarSolicitud(
                correoPasajero,
                dto,
                null
        );
    }

    // =============================================
    // 2. REGISTRAR TARJETA CON FOTO
    // =============================================

    @Override
    @Transactional
    public tarjetaResponseDTO registrarTarjetaConFoto(
            String correoPasajero,
            tarjetaDTO dto,
            MultipartFile fotoEvidencia
    ) {

        validarFoto(fotoEvidencia);

        return guardarSolicitud(
                correoPasajero,
                dto,
                fotoEvidencia
        );
    }

    // =============================================
    // 3. GUARDAR SOLICITUD DE VINCULACIÓN
    // =============================================

    private tarjetaResponseDTO guardarSolicitud(
            String correoPasajero,
            tarjetaDTO dto,
            MultipartFile fotoEvidencia
    ) {

        // Validar datos recibidos
        if (dto == null || dto.numeroSerie() == null) {
            throw new BusinessRuleException(
                    "Debes ingresar el número de serie"
            );
        }

        String serial = dto.numeroSerie()
                .replaceAll("\\s+", "");

        // Validar exactamente 16 dígitos
        if (!serial.matches("^[0-9]{16}$")) {
            throw new BusinessRuleException(
                    "El serial debe tener exactamente 16 dígitos"
            );
        }

        // Obtener siempre el usuario desde el JWT
        if (correoPasajero == null ||
                correoPasajero.isBlank()) {
            throw new BusinessRuleException(
                    "No se pudo identificar al usuario"
            );
        }

        Usuario usuario = usuarioRepository
                .findByEmail(correoPasajero.trim())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario autenticado no encontrado"
                        )
                );

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new BusinessRuleException(
                    "Tu cuenta se encuentra deshabilitada"
            );
        }

        // Evitar que una tarjeta se vincule a otra cuenta
        if (tarjetaRepository.existsByNumeroSerie(serial)) {
            throw new DuplicateResourceException(
                    "Esta tarjeta ya está registrada en MOVI+"
            );
        }

        // Buscar tarjeta anterior del pasajero
        Tarjeta existente = tarjetaRepository
                .findByUsuarioId(usuario.getId())
                .orElse(null);

        // No reemplazar una tarjeta operativamente activa
        if (existente != null &&
                existente.getEstado() == estadoTarjeta.ACTIVA) {

            throw new BusinessRuleException(
                    "Ya tienes una tarjeta registrada. " +
                            "Debes bloquearla antes de solicitar otra."
            );
        }

        // Fotografía opcional para la ruta sin foto
        String urlEvidencia = null;

        if (fotoEvidencia != null &&
                !fotoEvidencia.isEmpty()) {

            validarFoto(fotoEvidencia);

            urlEvidencia = supabaseStorageService
                    .subirArchivo(
                            fotoEvidencia,
                            "tarjetas-pendientes"
                    );
        }

        LocalDateTime ahora = LocalDateTime.now();

        Tarjeta tarjeta;

        if (existente != null) {

            // Reutilizar registro anterior bloqueado
            // para respetar la relación OneToOne actual
            tarjeta = existente;

            tarjeta.setNumeroSerie(serial);
            tarjeta.setEstado(estadoTarjeta.ACTIVA);

        } else {

            // Primera solicitud de vinculación
            tarjeta = Tarjeta.builder()
                    .usuario(usuario)
                    .numeroSerie(serial)
                    .estado(estadoTarjeta.ACTIVA)
                    .build();
        }

        // Nunca marcar como verificada automáticamente
        tarjeta.setEstadoVerificacion(
                EstadoVerificacionTarjeta.PENDIENTE
        );

        tarjeta.setFotoEvidenciaUrl(urlEvidencia);
        tarjeta.setFechaSolicitud(ahora);
        tarjeta.setFechaVerificacion(null);
        tarjeta.setObservacionesRevision(null);
        tarjeta.setFechaVinculacion(ahora);

        Tarjeta guardada = tarjetaRepository.save(tarjeta);

        return tarjetaResponseDTO.desde(guardada);
    }

    // =============================================
    // 4. VALIDAR FOTOGRAFÍA
    // =============================================

    private void validarFoto(MultipartFile foto) {

        if (foto == null || foto.isEmpty()) {
            throw new BusinessRuleException(
                    "Debes adjuntar una fotografía de la tarjeta"
            );
        }

        // Máximo 5 MB
        if (foto.getSize() > 5L * 1024 * 1024) {
            throw new BusinessRuleException(
                    "La fotografía no puede superar los 5 MB"
            );
        }

        String tipo = foto.getContentType();

        if (tipo == null ||
                !(tipo.equals("image/jpeg") ||
                        tipo.equals("image/png") ||
                        tipo.equals("image/webp"))) {

            throw new BusinessRuleException(
                    "La imagen debe ser JPG, PNG o WEBP"
            );
        }

        // Para producción también debe validarse
        // el contenido real del archivo, no solo MIME.
    }

    // =============================================
    // 5. OBTENER TARJETA POR ID
    // =============================================

    @Override
    @Transactional(readOnly = true)
    public Tarjeta obtenerPorId(Long id) {

        return tarjetaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Tarjeta no encontrada con ID " + id
                        )
                );
    }

    // =============================================
    // 6. OBTENER TARJETA POR USUARIO
    // =============================================

    @Override
    @Transactional(readOnly = true)
    public Tarjeta obtenerPorUsuario(Long usuarioId) {

        return tarjetaRepository
                .findByUsuarioId(usuarioId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "El usuario no tiene una tarjeta registrada"
                        )
                );
    }

    // =============================================
    // 7. LISTAR TARJETAS
    // =============================================

    @Override
    @Transactional(readOnly = true)
    public List<Tarjeta> listar() {
        return tarjetaRepository.findAll();
    }

    // =============================================
    // 8. CAMBIAR ESTADO OPERATIVO
    // =============================================

    @Override
    @Transactional
    public Tarjeta cambiarEstado(
            Long id,
            estadoTarjeta nuevoEstado
    ) {

        if (nuevoEstado == null) {
            throw new BusinessRuleException(
                    "El estado es obligatorio"
            );
        }

        Tarjeta tarjeta = obtenerPorId(id);

        tarjeta.setEstado(nuevoEstado);

        return tarjetaRepository.save(tarjeta);
    }

    // =============================================
    // 9. BUSCAR POR USUARIO Y ESTADO
    // =============================================

    @Override
    @Transactional(readOnly = true)
    public Tarjeta obtenerPorUsuarioYEstado(
            Long usuarioId,
            estadoTarjeta estado
    ) {

        if (estado == null) {
            throw new BusinessRuleException(
                    "El estado es obligatorio"
            );
        }

        return tarjetaRepository
                .findByUsuario_IdAndEstado(
                        usuarioId,
                        estado
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No se encontró una tarjeta " +
                                        "con el estado solicitado"
                        )
                );
    }

    // =============================================
    // 10. ELIMINAR TARJETA
    // =============================================

    @Override
    @Transactional
    public void eliminar(Long id) {

        Tarjeta tarjeta = obtenerPorId(id);

        tarjetaRepository.delete(tarjeta);
    }

    // =============================================
    // 11. BLOQUEAR TARJETA ACTUAL
    // =============================================

    @Override
    @Transactional
    public void bloquearTarjetaActual(
            String correoPasajero
    ) {

        Usuario usuario = usuarioRepository
                .findByEmail(correoPasajero.trim())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario no encontrado"
                        )
                );

        Tarjeta tarjetaActiva = tarjetaRepository
                .findByUsuario_IdAndEstado(
                        usuario.getId(),
                        estadoTarjeta.ACTIVA
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No tienes una tarjeta activa para bloquear"
                        )
                );

        tarjetaActiva.setEstado(
                estadoTarjeta.BLOQUEADA
        );

        tarjetaRepository.save(tarjetaActiva);
    }

    // =============================================
    // 12. CONSULTAR TARJETA ACTIVA
    // =============================================

    @Override
    @Transactional(readOnly = true)
    public tarjetaResponseDTO obtenerTarjetaActiva(
            String correoPasajero
    ) {

        Usuario usuario = usuarioRepository
                .findByEmail(correoPasajero.trim())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuario no encontrado"
                        )
                );

        Tarjeta tarjeta = tarjetaRepository
                .findByUsuario_IdAndEstado(
                        usuario.getId(),
                        estadoTarjeta.ACTIVA
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No tienes una tarjeta activa"
                        )
                );

        return tarjetaResponseDTO.desde(tarjeta);
    }
}
