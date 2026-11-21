package com.example.MOVI.DTO.ResponseDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record viajePruebaRequest(
        @NotBlank(message = "Selecciona una estación") String nombreEstacion,
        @NotBlank(message = "Selecciona el componente")
        @Pattern(regexp = "TRONCAL|ZONAL", flags = Pattern.Flag.CASE_INSENSITIVE,
                 message = "El componente debe ser TRONCAL o ZONAL") String tipoComponente
) {}
