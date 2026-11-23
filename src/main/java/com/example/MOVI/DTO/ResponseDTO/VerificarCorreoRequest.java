package com.example.MOVI.DTO.ResponseDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerificarCorreoRequest(
        @Email @NotBlank String correoElectronico,
        @NotBlank
        @Pattern(regexp = "\\d{6}")
        String codigo
) {}
