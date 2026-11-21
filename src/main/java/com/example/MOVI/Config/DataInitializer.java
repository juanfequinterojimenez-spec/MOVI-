package com.example.MOVI.Config;

import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Enum.Rol;
import com.example.MOVI.Repository.usuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final usuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.email:admin@movi.com}")
    private String correoAdmin;

    @Value("${admin.default.password}")
    private String passwordAdmin;

    @Bean
    public CommandLineRunner initAdmin() {

        return args -> {

            // Si el administrador ya existe,
            // no modificar su contraseña ni sus datos.
            if (usuarioRepository.findByEmail(correoAdmin).isPresent()) {
                System.out.println(
                        "Administrador existente. No se realizaron cambios."
                );
                return;
            }

            if (passwordAdmin == null || passwordAdmin.isBlank()) {
                throw new IllegalStateException(
                        "Falta configurar ADMIN_PASSWORD."
                );
            }

            Usuario admin = Usuario.builder()
                    .nombreCompleto("Administrador MOVI")
                    .email(correoAdmin)
                    .rol(Rol.ADMINISTRADOR)
                    .puntos(0)
                    .password(passwordEncoder.encode(passwordAdmin))
                    .build();

            usuarioRepository.save(admin);

            System.out.println(
                    "Administrador inicial creado correctamente."
            );
        };
    }
}
