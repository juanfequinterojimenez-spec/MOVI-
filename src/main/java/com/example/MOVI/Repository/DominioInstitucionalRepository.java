
package com.example.MOVI.Repository;

import com.example.MOVI.Entity.DominioInstitucional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DominioInstitucionalRepository
        extends JpaRepository<DominioInstitucional, Long> {

    Optional<DominioInstitucional>
    findByDominioIgnoreCaseAndActivoTrueAndVerificadoTrue(
            String dominio
    );
}

