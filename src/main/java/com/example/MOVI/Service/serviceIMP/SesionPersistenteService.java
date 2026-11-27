package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.Config.JwtService;
import com.example.MOVI.DTO.ResponseDTO.SesionPersistenteResponse;
import com.example.MOVI.DTO.ResponseDTO.usuarioResponseDTO;
import com.example.MOVI.Entity.SesionPersistente;
import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Repository.SesionPersistenteRepository;
import com.example.MOVI.Repository.usuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class SesionPersistenteService {
    private static final Duration DURACION = Duration.ofDays(30);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SesionPersistenteRepository sesiones;
    private final usuarioRepository usuarios;
    private final JwtService jwtService;

    @Transactional
    public SesionPersistenteResponse activar(Long idUsuario) {
        Usuario usuario = usuarios.findById(idUsuario)
                .orElseThrow(() -> new BadCredentialsException("Sesión no válida"));
        comprobarUsuario(usuario);
        Instant ahora = Instant.now();
        String nuevoToken = generarSecreto();
        SesionPersistente sesion = new SesionPersistente();
        sesion.setUsuario(usuario);
        sesion.setCreadaEn(ahora);
        sesion.setVenceEn(ahora.plus(DURACION));
        sesion.setTokenHash(sha256(nuevoToken));
        sesiones.save(sesion);
        return respuesta(usuario, nuevoToken, sesion.getVenceEn());
    }

    @Transactional
    public SesionPersistenteResponse renovar(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadCredentialsException("Sesión vencida. Inicia sesión nuevamente");
        }
        SesionPersistente sesion = sesiones.findByTokenHash(sha256(refreshToken))
                .orElseThrow(() -> new BadCredentialsException("Sesión vencida. Inicia sesión nuevamente"));
        Instant ahora = Instant.now();
        if (sesion.getRevocadaEn() != null || !ahora.isBefore(sesion.getVenceEn())) {
            throw new BadCredentialsException("Sesión vencida. Inicia sesión nuevamente");
        }
        Usuario usuario = sesion.getUsuario();
        comprobarUsuario(usuario);

        // Rotación sin extender el límite absoluto de 30 días.
        String nuevoRefresh = generarSecreto();
        sesion.setTokenHash(sha256(nuevoRefresh));
        sesiones.save(sesion);
        return respuesta(usuario, nuevoRefresh, sesion.getVenceEn());
    }

    @Transactional
    public void revocar(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        sesiones.findByTokenHash(sha256(refreshToken)).ifPresent(sesion -> {
            sesion.setRevocadaEn(Instant.now());
            sesiones.save(sesion);
        });
    }

    private SesionPersistenteResponse respuesta(Usuario usuario, String refreshToken, Instant venceEn) {
        return new SesionPersistenteResponse(
                jwtService.generarToken(usuario),
                refreshToken,
                venceEn,
                usuarioResponseDTO.desde(usuario)
        );
    }

    private void comprobarUsuario(Usuario usuario) {
        if (!Boolean.TRUE.equals(usuario.getActivo()) ||
                !Boolean.TRUE.equals(usuario.getCorreoVerificado())) {
            throw new BadCredentialsException("Debes volver a autenticar tu cuenta");
        }
    }

    private static String generarSecreto() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256(String valor) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
