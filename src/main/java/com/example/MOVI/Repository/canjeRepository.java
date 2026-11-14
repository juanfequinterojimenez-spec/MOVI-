package com.example.MOVI.Repository;


import com.example.MOVI.Entity.Canje;

import com.example.MOVI.Enum.EstadoCanje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface canjeRepository extends JpaRepository<Canje, Long> {

    Optional<Canje> findByCodigoComprobante(String codigoComprobante);
    // 🆕 Retorna una página de canjes ordenados por fecha descendente
    Page<Canje> findByUsuario_IdOrderByFechaCanjeDesc(Long usuarioId, Pageable pageable);

    // 🆕 Retorna una página filtrando por usuario y estado
    Page<Canje> findByUsuario_IdAndEstadoOrderByFechaCanjeDesc(Long usuarioId, EstadoCanje estado, Pageable pageable);
}
