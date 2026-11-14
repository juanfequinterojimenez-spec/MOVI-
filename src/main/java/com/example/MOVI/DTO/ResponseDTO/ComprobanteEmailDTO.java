package com.example.MOVI.DTO.ResponseDTO;

import com.example.MOVI.Entity.Canje;

import java.time.LocalDateTime;

public record ComprobanteEmailDTO(
        String correoDestino,
        String nombrePasajero,
        String tituloRecompensa,
        String nombreAliado,
        Integer puntosGastados,
        LocalDateTime fechaCanje,
        LocalDateTime fechaVencimiento,
        String codigoComprobante,
        String estado
) {
    public static ComprobanteEmailDTO desde(Canje canje) {
        String recompensa = (canje.getRecompensa() != null)
                ? canje.getRecompensa().getTitulo()
                : "Beneficio MOVI";

        String aliado = (canje.getRecompensa() != null && canje.getRecompensa().getAliado() != null)
                ? canje.getRecompensa().getAliado().getNombreAliado()
                : "Aliado MOVI";

        return new ComprobanteEmailDTO(
                canje.getUsuario().getEmail(),
                canje.getUsuario().getNombreCompleto(),
                recompensa,
                aliado,
                canje.getPuntosGastados(),
                canje.getFechaCanje(),
                canje.getFechaVencimiento(),
                canje.getCodigoComprobante(),
                canje.getEstado().name()
        );
    }
}
