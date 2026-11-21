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

    @Override
    public void run(String... args) {

        // Si el administrador ya existe, no modificarlo.
        if (usuarioRepository.existsByEmail(correoAdmin)) {
            log.info("El administrador ya está registrado.");
            return;
        }

        // Sin contraseña configurada, no crear
        // un administrador inseguro.
        if (passwordAdmin == null || passwordAdmin.isBlank()) {
            log.warn(
                    "No se creó el administrador: falta ADMIN_PASSWORD."
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