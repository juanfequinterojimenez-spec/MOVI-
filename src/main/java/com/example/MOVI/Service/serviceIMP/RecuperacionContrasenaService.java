package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.DTO.ResponseDTO.*;
import com.example.MOVI.Entity.*;
import com.example.MOVI.Repository.*;
import com.example.MOVI.Exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RecuperacionContrasenaService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final usuarioRepository usuarios;
    private final IdentidadExternaRepository identidades;
    private final RecuperacionContrasenaRepository recuperaciones;
    private final SesionPersistenteRepository sesiones;
    private final PasswordEncoder encoder;
    private final CorreoService correos;

    // Respuesta uniforme: no confirma si una cuenta existe.
    @Transactional
    public void solicitar(SolicitarRecuperacionRequest request) {
        String correo = normalizar(request.correoElectronico());
        usuarios.findByEmail(correo).ifPresent(usuario -> {
            if (!Boolean.TRUE.equals(usuario.getActivo()) ||
                !Boolean.TRUE.equals(usuario.getCorreoVerificado()) ||
                identidades.existsByUsuarioId(usuario.getId())) return;

            Instant ahora = Instant.now();
            RecuperacionContrasena registro = recuperaciones.findByUsuarioId(usuario.getId())
                .orElseGet(() -> { var nuevo = new RecuperacionContrasena(); nuevo.setUsuario(usuario); return nuevo; });
            // No indicar al cliente que hay un cooldown, para evitar enumeración.
            if (registro.getUltimoEnvio() != null &&
                Duration.between(registro.getUltimoEnvio(), ahora).getSeconds() < 60) return;

            String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
            registro.setCodigoHash(encoder.encode(codigo));
            registro.setVenceEn(ahora.plus(Duration.ofMinutes(10)));
            registro.setUltimoEnvio(ahora);
            registro.setIntentos(0);
            recuperaciones.save(registro);
            correos.enviarCodigoRecuperacion(usuario.getEmail(), codigo);
        });
    }

    @Transactional(noRollbackFor = BusinessRuleException.class)
    public void confirmar(ConfirmarRecuperacionRequest request) {
        String error = "Código inválido o vencido";
        Usuario usuario = usuarios.findByEmail(normalizar(request.correoElectronico()))
            .orElseThrow(() -> new BusinessRuleException(error));
        if (!Boolean.TRUE.equals(usuario.getActivo()) || !Boolean.TRUE.equals(usuario.getCorreoVerificado()) ||
            identidades.existsByUsuarioId(usuario.getId())) throw new BusinessRuleException(error);
        RecuperacionContrasena registro = recuperaciones.findByUsuarioId(usuario.getId())
            .orElseThrow(() -> new BusinessRuleException(error));
        if (!Instant.now().isBefore(registro.getVenceEn()) || registro.getIntentos() >= 5)
            throw new BusinessRuleException(error);
        if (!encoder.matches(request.codigo(), registro.getCodigoHash())) {
            registro.setIntentos(registro.getIntentos() + 1);
            recuperaciones.save(registro);
            throw new BusinessRuleException(error);
        }
        usuario.setPassword(encoder.encode(request.nuevaContrasena()));
        usuarios.save(usuario);
        recuperaciones.delete(registro);
        // Invalidar TODOS los refresh tokens vinculados a la cuenta.
        sesiones.revocarTodasDeUsuario(usuario.getId(), Instant.now());
    }

    private String normalizar(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
