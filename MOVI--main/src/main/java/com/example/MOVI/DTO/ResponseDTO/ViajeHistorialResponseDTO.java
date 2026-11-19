package com.example.MOVI.DTO.ResponseDTO;

import com.example.MOVI.Entity.Viaje;

import java.time.LocalDateTime;

public record ViajeHistorialResponseDTO(
        Long idViaje,
        String nombreEstacion,
        Integer puntosGenerados,
        String tipoComponente,
        LocalDateTime fechaHoraViaje
) {
    public static ViajeHistorialResponseDTO desde(Viaje viaje) {
        return new ViajeHistorialResponseDTO(
                viaje.getIdViaje(),
                viaje.getNombreEstacion(),
                viaje.getPuntosGenerados(),
                viaje.getTipoComponente(),
                viaje.getFechaHoraViaje()

        );
    }
}