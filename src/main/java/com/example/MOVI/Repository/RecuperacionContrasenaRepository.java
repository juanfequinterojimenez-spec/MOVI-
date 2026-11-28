package com.example.MOVI.Repository;

import com.example.MOVI.Entity.RecuperacionContrasena;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RecuperacionContrasenaRepository extends JpaRepository<RecuperacionContrasena, UUID> {
    Optional<RecuperacionContrasena> findByUsuarioId(Long id);
}
