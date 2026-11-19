package com.example.MOVI.Repository;

import com.example.MOVI.Entity.Aliado;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface aliadoRepository extends JpaRepository<Aliado, Long> {
    Optional<Aliado> findByUsuarioId(Long id);
}
