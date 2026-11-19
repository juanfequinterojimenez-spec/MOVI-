package com.example.MOVI.DTO.ResponseDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record viajeSimulacionDTO(
        @NotNull(message = "El ID del usuario es obligatorio")
        Long idUsuario,

        @NotNull(message = "El ID de la tarjeta es obligatorio")
        Long idTarjeta,

        @NotBlank(message = "El nombre de la estación o portal es obligatorio")
        String nombreEstacion,

        String tipoComponente
) {}
