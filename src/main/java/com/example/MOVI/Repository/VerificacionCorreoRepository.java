package com.example.MOVI.Repository;

import com.example.MOVI.Entity.VerificacionCorreo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificacionCorreoRepository
        extends JpaRepository<VerificacionCorreo, UUID> {

    Optional<VerificacionCorreo> findByUsuarioId(Long usuarioId);
}
