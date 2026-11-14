package com.example.MOVI.Repository;

import com.example.MOVI.Entity.IdentidadExterna;
import com.example.MOVI.Enum.ProveedorIdentidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdentidadExternaRepository
        extends JpaRepository<IdentidadExterna, Long> {

    Optional<IdentidadExterna> findByProveedorAndSubject(
            ProveedorIdentidad proveedor,
            String subject
    );
}
