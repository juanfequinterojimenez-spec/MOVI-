package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.Config.JwtService;
import com.example.MOVI.DTO.ResponseDTO.*;
import com.example.MOVI.Entity.Canje;
import com.example.MOVI.Entity.Recompensa;
import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Enum.EstadoCanje;
import com.example.MOVI.Exception.BusinessRuleException;
import com.example.MOVI.Exception.ResourceNotFoundException;
import com.example.MOVI.Repository.canjeRepository;
import com.example.MOVI.Repository.recompensaRepository;
import com.example.MOVI.Repository.usuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class canjeServiceIMP implements canjeService {

    private final canjeRepository canjeRepository;
    private final usuarioRepository usuarioRepository;
    private final recompensaRepository recompensaRepository;
    private final JwtService jwtService;
    private final ComprobantePdfService comprobantePdfService;
    private final EmailService emailService;

    @Override
    @Transactional
    public CanjeResponseDto procesarCanje(String correoPasajero, Long recompensaId) {
        // 1. Buscar al pasajero
        Usuario pasajero = usuarioRepository.findByEmail(correoPasajero)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 2. Buscar la recompensa
        Recompensa recompensa = recompensaRepository.findById(recompensaId)
                .orElseThrow(() -> new ResourceNotFoundException("Recompensa no encontrada"));

        // 3. Validar reglas de negocio (Stock y Disponibilidad)
        if (!recompensa.getActiva()) {
            throw new BusinessRuleException("Esta oferta ya no se encuentra activa.");
        }

        if (recompensa.getStockDisponible() <= 0) {
            throw new BusinessRuleException("Se han agotado los cupones para esta oferta.");
        }

        // 4. Validar si el usuario tiene puntos suficientes
        if (pasajero.getPuntos() < recompensa.getCostoPuntos()) {
            throw new BusinessRuleException("Puntos insuficientes. Necesitas " + recompensa.getCostoPuntos() +
                    " puntos, pero tienes " + pasajero.getPuntos());
        }

        // 5. Aplicar descuentos de puntos y stock
        pasajero.setPuntos(pasajero.getPuntos() - recompensa.getCostoPuntos());
        recompensa.setStockDisponible(recompensa.getStockDisponible() - 1);

        if (recompensa.getStockDisponible() == 0) {
            recompensa.setActiva(false);
        }

        // 6. Construir el ticket/cupón de canje con 30 días de vigencia
        Canje nuevoCanje = Canje.builder()
                .usuario(pasajero)
                .recompensa(recompensa)
                .puntosGastados(recompensa.getCostoPuntos())
                .codigoComprobante(generarCodigoUnico())
                .estado(EstadoCanje.VIGENTE)
                .fechaVencimiento(LocalDateTime.now().plusDays(30))
                .build();

        // 7. Guardar en BD
        usuarioRepository.save(pasajero);
        recompensaRepository.save(recompensa);
        Canje canjeGuardado = canjeRepository.save(nuevoCanje);

        // 8. Mapear datos planos dentro de la transacción y enviar correo asíncrono con QR + PDF
        ComprobanteEmailDTO datosCorreo = ComprobanteEmailDTO.desde(canjeGuardado);
        emailService.enviarComprobanteCanje(datosCorreo);

        return CanjeResponseDto.desde(canjeGuardado);
    }

    @Override
    @Transactional
    public CanjeResponseDto validarCupon(String correoAliado, String codigoComprobante) {
        // 1. Verificar que el aliado autenticado exista
        Usuario aliadoAutenticado = usuarioRepository.findByEmail(correoAliado)
                .orElseThrow(() -> new ResourceNotFoundException("Aliado no autorizado."));

        // 2. Buscar el cupón por el código leído desde el QR
        Canje canje = canjeRepository.findByCodigoComprobante(codigoComprobante)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón no encontrado con código: " + codigoComprobante));

        // 3. Validar que el cupón pertenezca a una recompensa de ESTE aliado
        if (canje.getRecompensa() == null || canje.getRecompensa().getAliado() == null ||
                canje.getRecompensa().getAliado().getUsuario() == null ||
                !canje.getRecompensa().getAliado().getUsuario().getId().equals(aliadoAutenticado.getId())) {
            throw new BusinessRuleException("Este cupón pertenece a otro comercio aliado y no puede ser validado por tu cuenta.");
        }

        // 4. Validar que no haya sido utilizado antes
        if (EstadoCanje.UTILIZADO.equals(canje.getEstado())) {
            throw new BusinessRuleException("Este cupón ya fue utilizado previamente.");
        }

        // 5. Validar que no haya expirado por fecha
        if (canje.getFechaVencimiento() != null && LocalDateTime.now().isAfter(canje.getFechaVencimiento())) {
            canje.setEstado(EstadoCanje.EXPIRADO);
            canjeRepository.save(canje);
            throw new BusinessRuleException("Este cupón expiró el " +
                    canje.getFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }

        // 6. Marcar el cupón como UTILIZADO
        canje.setEstado(EstadoCanje.UTILIZADO);
        canje.setFechaEntrega(LocalDateTime.now());
        Canje canjeActualizado = canjeRepository.save(canje);

        return CanjeResponseDto.desde(canjeActualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public QrTemporalResponseDTO generarQrTemporal(String correoPasajero) {
        Usuario usuario = usuarioRepository.findByEmail(correoPasajero)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correoPasajero));

        // 2 minutos de duración para evitar fraudes o capturas de pantalla
        long tiempoExpiracionMs = 2L * 60 * 1000;
        String tokenQr = jwtService.generarTokenRedencion(usuario.getId(), tiempoExpiracionMs);

        return new QrTemporalResponseDTO(
                tokenQr,
                120,
                LocalDateTime.now().plusMinutes(2)
        );
    }

    @Override
    @Transactional
    public RedencionResponseDTO redimirPuntos(RedencionPuntosDTO request, String correoAliado) {
        // 1. Buscar al pasajero
        Usuario pasajero = usuarioRepository.findById(request.idUsuarioPasajero())
                .orElseThrow(() -> new ResourceNotFoundException("Pasajero no encontrado con ID: " + request.idUsuarioPasajero()));

        // 2. Buscar la recompensa
        Recompensa recompensa = recompensaRepository.findById(request.idBeneficio())
                .orElseThrow(() -> new ResourceNotFoundException("Recompensa no encontrada con ID: " + request.idBeneficio()));

        // 3. Validar saldo suficiente
        if (pasajero.getPuntos() < recompensa.getCostoPuntos()) {
            throw new BusinessRuleException("Puntos insuficientes: El pasajero cuenta con " + pasajero.getPuntos() +
                    " puntos, pero la recompensa requiere " + recompensa.getCostoPuntos() + ".");
        }

        // 4. Descontar puntos y stock
        pasajero.setPuntos(pasajero.getPuntos() - recompensa.getCostoPuntos());
        if (recompensa.getStockDisponible() != null && recompensa.getStockDisponible() > 0) {
            recompensa.setStockDisponible(recompensa.getStockDisponible() - 1);
            if (recompensa.getStockDisponible() == 0) {
                recompensa.setActiva(false);
            }
            recompensaRepository.save(recompensa);
        }
        usuarioRepository.save(pasajero);

        // 5. Guardar el registro de redención presencial ya como UTILIZADO
        Canje nuevoCanje = Canje.builder()
                .usuario(pasajero)
                .recompensa(recompensa)
                .puntosGastados(recompensa.getCostoPuntos())
                .codigoComprobante(generarCodigoUnico())
                .estado(EstadoCanje.UTILIZADO)
                .fechaVencimiento(LocalDateTime.now())
                .build();

        Canje canjeGuardado = canjeRepository.save(nuevoCanje);

        // 6. Enviar recibo PDF por correo
        ComprobanteEmailDTO datosCorreo = ComprobanteEmailDTO.desde(canjeGuardado);
        emailService.enviarComprobanteCanje(datosCorreo);

        return new RedencionResponseDTO(
                canjeGuardado.getIdCanje(),
                pasajero.getId(),
                canjeGuardado.getPuntosGastados(),
                pasajero.getPuntos(),
                canjeGuardado.getFechaCanje(),
                "Redención exitosa. Comprobante generado: " + canjeGuardado.getCodigoComprobante()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CanjeResponseDto> obtenerBilleteraPorEstado(String correoPasajero, String estado, Pageable pageable) {
        Usuario usuario = usuarioRepository.findByEmail(correoPasajero)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con correo: " + correoPasajero));

        if (estado != null && !estado.isBlank()) {
            EstadoCanje estadoEnum;
            try {
                estadoEnum = EstadoCanje.valueOf(estado.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleException("Estado de canje inválido: " + estado + ". Valores permitidos: VIGENTE, UTILIZADO, EXPIRADO.");
            }

            return canjeRepository.findByUsuario_IdAndEstadoOrderByFechaCanjeDesc(
                    usuario.getId(),
                    estadoEnum,
                    pageable
            ).map(CanjeResponseDto::desde);
        }

        return canjeRepository.findByUsuario_IdOrderByFechaCanjeDesc(usuario.getId(), pageable)
                .map(CanjeResponseDto::desde);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarPdfDeCanje(Long canjeId, String correoPasajero) {
        Canje canje = canjeRepository.findById(canjeId)
                .orElseThrow(() -> new ResourceNotFoundException("Canje no encontrado con ID: " + canjeId));

        if (!canje.getUsuario().getEmail().equalsIgnoreCase(correoPasajero)) {
            throw new BusinessRuleException("No tienes permiso para descargar el comprobante de este canje.");
        }

        return comprobantePdfService.generarComprobanteCanje(ComprobanteEmailDTO.desde(canje));
    }

    private String generarCodigoUnico() {
        String uuidCorto = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "MOVI-" + uuidCorto;
    }
}