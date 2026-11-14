package com.example.MOVI.Repository;

import com.example.MOVI.Entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface usuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByEmail(String email);

    Optional<Usuario> findByEmail(String email);

    boolean existsByDocumento(String documento);

    Optional<Usuario> findByDocumento(String documento);
}