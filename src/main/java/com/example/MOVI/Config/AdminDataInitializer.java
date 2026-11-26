
package com.example.MOVI.Config;

import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Enum.Rol;
import com.example.MOVI.Repository.usuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminDataInitializer implements CommandLineRunner {

    private final usuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.email:admin@movi.com}")
    private String correoAdmin;

    @Value("${admin.default.password:}")
    private String passwordAdmin;

    @Value("${admin.reset.password:false}")
    private boolean resetPasswordAdmin;

    @Override
    public void run(String... args) {

        Optional<Usuario> usuarioExistente =
                usuarioRepository.findByEmail(correoAdmin);

        // ==========================================
        // ADMINISTRADOR EXISTENTE
        // ==========================================

        if (usuarioExistente.isPresent()) {

            Usuario admin = usuarioExistente.get();

            // Protección: no modificar un usuario
            // que no tenga el rol de administrador.
            if (admin.getRol() != Rol.ADMINISTRADOR) {
                log.error(
                        "El correo configurado pertenece a una cuenta que no es administradora."
                );
                return;
            }

            if (!resetPasswordAdmin) {
                log.info(
                        "El administrador ya está registrado. No se realizaron cambios."
                );
                return;
            }

            if (passwordAdmin == null || passwordAdmin.isBlank()) {
                log.error(
                        "No se pudo restablecer la contraseña: falta ADMIN_DEFAULT_PASSWORD."
                );
                return;
            }

            // Evitar actualizar el hash si ya
            // corresponde a la nueva contraseña.
            if (admin.getPassword() != null &&
                    passwordEncoder.matches(
                            passwordAdmin,
                            admin.getPassword()
                    )) {

                log.info(
                        "La contraseña del administrador ya está actualizada."
                );
                return;
            }

            // Solo cambiar la contraseña.
            // Conservar ID, correo, rol y demás datos.
            admin.setPassword(
                    passwordEncoder.encode(passwordAdmin)
            );

            usuarioRepository.save(admin);

            log.info(
                    "Contraseña del administrador restablecida correctamente."
            );

            return;
        }

        // ==========================================
        // CREAR ADMINISTRADOR SI NO EXISTE
        // ==========================================

        if (passwordAdmin == null || passwordAdmin.isBlank()) {
            log.warn(
                    "No se creó el administrador: falta ADMIN_DEFAULT_PASSWORD."
            );
            return;
        }

        Usuario admin = Usuario.builder()
                .email(correoAdmin)
                .password(passwordEncoder.encode(passwordAdmin))
                .rol(Rol.ADMINISTRADOR)
                .documento("0000000000")
                .tipoDocumento("ADMIN")
                .nombreCompleto("Administrador Sistema")
                .puntos(0)
                .build();

        usuarioRepository.save(admin);

        log.info(
                "Administrador creado correctamente: {}",
                correoAdmin
        );
    }
}
