package com.example.MOVI.Config;

import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Enum.Rol; // Ajusta el nombre/paquete de tu enum de roles si es distinto
import com.example.MOVI.Repository.usuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final usuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initAdmin() {
        return args -> {
            String correoAdmin = "admin@movi.com";

            Usuario admin = usuarioRepository.findByEmail(correoAdmin)
                    .orElseGet(() -> Usuario.builder()
                            .nombreCompleto("Administrador MOVI")
                            .email(correoAdmin)
                            .rol(Rol.ADMINISTRADOR)
                            .puntos(0)
                            .build());

            admin.setRol(Rol.ADMINISTRADOR);
            admin.setPassword(passwordEncoder.encode("Admin1234*"));
            usuarioRepository.save(admin);

            System.out.println(">>> ADMIN RECREADO CORRECTAMENTE: " + correoAdmin + " / Admin1234*");
        };
    }
}
