package com.example.MOVI.Repository;


import com.example.MOVI.Entity.Tarjeta;
import com.example.MOVI.Entity.Viaje;
import com.example.MOVI.Enum.estadoTarjeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface viajeRepository extends JpaRepository<Viaje, Long> {
    List<Viaje> findByUsuario_IdOrderByFechaHoraViajeDesc (Long idUsuario);
    long countByUsuario_IdAndFechaHoraViajeBetween(Long usuarioId, LocalDateTime inicioDia, LocalDateTime finDia);
    Optional<Viaje> findFirstByUsuario_IdOrderByFechaHoraViajeDesc(Long usuarioId);

    // Total de puntos otorgados por viajes, usado en las estadísticas del admin
    @Query("select coalesce(sum(v.puntosGenerados), 0) from Viaje v")
    Long sumarPuntosGenerados();

}
