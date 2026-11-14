package com.example.MOVI.Repository;

import com.example.MOVI.Entity.Tarjeta;
import com.example.MOVI.Enum.estadoTarjeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface tarjetaRepository extends JpaRepository<Tarjeta, Long> {
    Optional<Tarjeta> findByUsuarioId(Long usuarioId);

    // Saber si el usuario ya tiene tarjeta

    // Verificar si ya existe una tarjeta con ese número de serie
    boolean existsByNumeroSerie(String numeroSerie);

    // Tarjeta de un usuario filtrada por estado
    Optional<Tarjeta> findByUsuario_IdAndEstado(Long usuarioId, estadoTarjeta estado);
}
