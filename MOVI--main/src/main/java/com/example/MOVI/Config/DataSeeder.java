package com.example.MOVI.Config;

import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Enum.Rol;
import com.example.MOVI.Repository.usuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final usuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 1. Administrador por defecto
        if (!usuarioRepository.existsByEmail("admin@moviplus.com")) {
            Usuario admin = Usuario.builder()
                    .nombreCompleto("Administrador MOVI+")
                    .documento("100000001")
                    .email("admin@moviplus.com")
                    .password(passwordEncoder.encode("Admin123*"))
                    .rol(Rol.ADMINISTRADOR)
                    .puntos(0)
                    .build();
            usuarioRepository.save(admin);
        }

        // 2. Aliado por defecto (ej. Cine Colombia / Comercio socio)
        if (!usuarioRepository.existsByEmail("aliado@moviplus.com")) {
             Usuario aliado = Usuario.builder()
                    .nombreCompleto("Aliado Comercial MOVI+")
                    .documento("1000000002")
                    .email("aliado@moviplus.com")
                    .password(passwordEncoder.encode("Aliado123*"))
                    .rol(Rol.ALIADO)
                    .puntos(0)
                    .build();
            usuarioRepository.save(aliado);
        }

        // 3. Pasajero / Estudiante por defecto
        if (!usuarioRepository.existsByEmail("pasajero@moviplus.com")) {
            Usuario usuario = Usuario.builder()
                    .nombreCompleto("Juan Pasajero Estudiante")
                    .documento("1000000003")
                    .email("pasajero@moviplus.com")
                    .password(passwordEncoder.encode("Pasajero123*"))
                    .rol(Rol.PASAJERO)
                    .puntos(100)
                    .build();
            usuarioRepository.save(usuario);
        }
    }
}

