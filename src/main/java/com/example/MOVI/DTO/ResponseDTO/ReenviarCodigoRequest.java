package com.example.MOVI.DTO.ResponseDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ReenviarCodigoRequest(
        @Email @NotBlank String correoElectronico
) {}
