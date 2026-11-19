package com.example.MOVI.DTO.ResponseDTO;

import com.example.MOVI.Entity.Viaje;

import java.time.LocalDateTime;

public record viajeResponseDTO(
        Long idViaje,
        String nombreEstacion,
        Integer puntosGenerados,
        LocalDateTime fechaHoraViaje,
        Integer saldoPuntosTotal,
        boolean esTransbordoGratuito // Nuevo indicador para la app
) {
    public static viajeResponseDTO desde(Viaje viaje, Integer saldoTotal, boolean esTransbordo) {
        return new viajeResponseDTO(
                viaje.getIdViaje(),
                viaje.getNombreEstacion(),
                viaje.getPuntosGenerados(),
                viaje.getFechaHoraViaje(),
                saldoTotal,
                esTransbordo
        );
    }
}
