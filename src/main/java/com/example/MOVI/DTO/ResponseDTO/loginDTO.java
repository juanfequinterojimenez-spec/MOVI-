package com.example.MOVI.DTO.ResponseDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record loginDTO(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Debe ser un correo válido")
        String correoElectronico,

        @NotBlank(message = "La contraseña es obligatoria")
        String contrasena
) {}
