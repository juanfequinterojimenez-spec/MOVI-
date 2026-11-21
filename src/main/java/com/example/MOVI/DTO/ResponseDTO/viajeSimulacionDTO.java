package com.example.MOVI.DTO.ResponseDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record viajeSimulacionDTO(
    @NotNull(message = "El ID del usuario es obligatorio") Long idUsuario,
    @NotNull(message = "El ID de la tarjeta es obligatorio") Long idTarjeta,
    @NotBlank(message = "La estación es obligatoria") String nombreEstacion,
    @NotBlank(message = "El componente es obligatorio")
    @Pattern(regexp = "TRONCAL|ZONAL", flags = Pattern.Flag.CASE_INSENSITIVE,
             message = "El componente debe ser TRONCAL o ZONAL") String tipoComponente
) {}
