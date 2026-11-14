package com.example.MOVI.Repository;

import com.example.MOVI.Entity.Recompensa;
import com.example.MOVI.Enum.categoriaAliado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface recompensaRepository extends JpaRepository<Recompensa, Long> {
    List<Recompensa> findByAliado_Id(Long id);

    List<Recompensa> findByActivaTrue();

    List<Recompensa> findByActivaTrueAndAliado_CategoriaAliado(categoriaAliado categoria);
}

