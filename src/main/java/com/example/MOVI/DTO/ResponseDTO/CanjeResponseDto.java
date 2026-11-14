package com.example.MOVI.DTO.ResponseDTO;

import com.example.MOVI.Entity.Canje;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record CanjeResponseDto(
        Long idCanje,
        String tituloRecompensa,
        String nombreAliado,
        String imagenUrl,
        Integer puntosGastados,
        LocalDateTime fechaCanje,
        String codigoComprobante,
        com.example.MOVI.Enum.EstadoCanje estado,
        LocalDateTime vencimiento
) {

    public static CanjeResponseDto desde(Canje canje) {
        String titulo = (canje.getRecompensa() != null)
                ? canje.getRecompensa().getTitulo()
                : "Beneficio redimido";

        String aliado = (canje.getRecompensa() != null && canje.getRecompensa().getAliado() != null)
                ? canje.getRecompensa().getAliado().getNombreAliado()
                : "Aliado MOVI";

        String imagen = (canje.getRecompensa() != null)
                ? canje.getRecompensa().getImagenUrl()
                : null;
        String vencimiento = canje.getFechaVencimiento() != null
                ? canje.getFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "Sin vencimiento";

        return new CanjeResponseDto(
                canje.getIdCanje(),
                titulo,
                aliado,
                imagen,
                canje.getPuntosGastados(),
                canje.getFechaCanje(),
                canje.getCodigoComprobante(),
                canje.getEstado(),
                canje.getFechaVencimiento()
        );
    }
}