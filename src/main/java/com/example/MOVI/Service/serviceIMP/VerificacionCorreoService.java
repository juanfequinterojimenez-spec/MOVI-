package com.example.MOVI.Service.serviceIMP;


import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Entity.VerificacionCorreo;
import com.example.MOVI.Exception.BusinessRuleException;
import com.example.MOVI.Repository.VerificacionCorreoRepository;
import com.example.MOVI.Repository.usuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class VerificacionCorreoService {

    private final VerificacionCorreoRepository verificaciones;
    private final usuarioRepository usuarios;
    private final CorreoService correoService;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom random = new SecureRandom();

    @Transactional
    public void enviarCodigo(Usuario usuario) {
        if (Boolean.TRUE.equals(usuario.getCorreoVerificado())) {
            throw new BusinessRuleException(
                    "El correo ya está verificado"
            );
        }

        Instant ahora = Instant.now();

        VerificacionCorreo registro = verificaciones
                .findByUsuarioId(usuario.getId())
                .orElseGet(() -> {
                    VerificacionCorreo nuevo =
                            new VerificacionCorreo();
                    nuevo.setUsuario(usuario);
                    return nuevo;
                });

        if (registro.getUltimoEnvio() != null &&
                Duration.between(
                        registro.getUltimoEnvio(), ahora
                ).getSeconds() < 60) {

            throw new BusinessRuleException(
                    "Espera 60 segundos para reenviar el código"
            );
        }

        String codigo = String.format(
                "%06d", random.nextInt(1_000_000)
        );

        registro.setCodigoHash(
                passwordEncoder.encode(codigo)
        );
        registro.setExpiraEn(ahora.plusSeconds(600));
        registro.setUltimoEnvio(ahora);
        registro.setIntentos(0);

        verificaciones.save(registro);

        correoService.enviarCodigo(
                usuario.getEmail(), codigo
        );
    }

    @Transactional
    public Usuario verificar(String email, String codigo) {

        Usuario usuario = usuarios
                .findByEmail(email.trim().toLowerCase(
                        java.util.Locale.ROOT
                ))
                .orElseThrow(() -> new BusinessRuleException(
                        "Código inválido o vencido"
                ));

        if (Boolean.TRUE.equals(usuario.getCorreoVerificado())) {
            throw new BusinessRuleException(
                    "Esta cuenta ya está verificada"
            );
        }

        VerificacionCorreo registro = verificaciones
                .findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new BusinessRuleException(
                        "Código inválido o vencido"
                ));

        if (Instant.now().isAfter(registro.getExpiraEn())) {
            throw new BusinessRuleException(
                    "El código ha vencido. Solicita uno nuevo"
            );
        }

        if (registro.getIntentos() >= 5) {
            throw new BusinessRuleException(
                    "Superaste los intentos permitidos"
            );
        }

        if (!passwordEncoder.matches(
                codigo, registro.getCodigoHash()
        )) {
            registro.setIntentos(
                    registro.getIntentos() + 1
            );
            verificaciones.save(registro);

            throw new BusinessRuleException(
                    "Código incorrecto"
            );
        }

        usuario.setCorreoVerificado(true);
        usuarios.save(usuario);
        verificaciones.delete(registro);

        return usuario;
    }

    @Transactional
    public void reenviar(String email) {
        usuarios.findByEmail(
                email.trim().toLowerCase(java.util.Locale.ROOT)
        ).ifPresent(usuario -> {
            if (!Boolean.TRUE.equals(
                    usuario.getCorreoVerificado()
            )) {
                enviarCodigo(usuario);
            }
        });
    }
}
