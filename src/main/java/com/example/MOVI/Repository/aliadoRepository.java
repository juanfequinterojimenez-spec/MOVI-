package com.example.MOVI.Repository;

import com.example.MOVI.Entity.Aliado;
import com.example.MOVI.Enum.EstadoAliado;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface aliadoRepository extends JpaRepository<Aliado, Long> {
    Optional<Aliado> findByUsuarioId(Long id);

    List<Aliado> findByEstado(EstadoAliado estado);

    long countByEstado(EstadoAliado estado);
}
