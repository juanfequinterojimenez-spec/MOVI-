package com.example.MOVI.DTO.ResponseDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record tarjetaDTO(
        @NotBlank(message = "El número de serie es obligatorio")
        @Size(min = 16, max = 16, message = "El número de serie debe tener exactamente 16 dígitos")
        @Pattern(regexp = "^[0-9]{16}$", message = "El número de serie solo debe contener números")
        String numeroSerie
) {}
