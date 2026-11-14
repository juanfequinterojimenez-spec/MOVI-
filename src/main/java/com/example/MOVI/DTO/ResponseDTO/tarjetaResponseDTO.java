package com.example.MOVI.DTO.ResponseDTO;

import com.example.MOVI.Entity.Tarjeta;

import java.time.LocalDateTime;

public record tarjetaResponseDTO(
        Long idTarjeta,
        String numeroSerie,
        String estado,
        LocalDateTime fechaVinculacion
) {
    public static tarjetaResponseDTO desde(Tarjeta tarjeta) {
        return new tarjetaResponseDTO(
                tarjeta.getIdTarjeta(),
                tarjeta.getNumeroSerie(),
                tarjeta.getEstado() != null ? tarjeta.getEstado().name() : null,
                tarjeta.getFechaVinculacion()
        );
    }
}
