package com.example.MOVI.Repository;

import com.example.MOVI.Entity.Recompensa;
import com.example.MOVI.Enum.categoriaAliado;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface recompensaRepository
        extends JpaRepository<Recompensa, Long> {

    @EntityGraph(attributePaths = {"aliado"})
    List<Recompensa> findByAliado_Id(Long id);

    @EntityGraph(attributePaths = {"aliado"})
    List<Recompensa> findByActivaTrue();

    @EntityGraph(attributePaths = {"aliado"})
    List<Recompensa> findByActivaTrueAndAliado_CategoriaAliado(
            categoriaAliado categoria
    );
}

