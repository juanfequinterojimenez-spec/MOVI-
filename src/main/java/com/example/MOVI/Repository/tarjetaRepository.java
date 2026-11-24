package com.example.MOVI.Repository;

import com.example.MOVI.Entity.Tarjeta;
import com.example.MOVI.Enum.EstadoVerificacionTarjeta;
import com.example.MOVI.Enum.estadoTarjeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface tarjetaRepository extends JpaRepository<Tarjeta, Long> {
    Optional<Tarjeta> findByUsuarioId(Long usuarioId);

    boolean existsByNumeroSerie(String numeroSerie);

    // Tarjeta de un usuario filtrada por estado
    Optional<Tarjeta> findByUsuario_IdAndEstado(Long usuarioId, estadoTarjeta estado);

    List<Tarjeta> findByEstadoVerificacionIn(
            Collection<EstadoVerificacionTarjeta> estados
    );
}
