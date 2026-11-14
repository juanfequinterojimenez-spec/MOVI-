package com.example.MOVI.DTO.ResponseDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record registerRequest(
        @NotBlank(message = "El nombre completo es obligatorio")
        String nombreCompleto,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El formato del correo electrónico no es válido")
        String correoElectronico,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String contrasena,

        @NotBlank(message = "El tipo de documento es obligatorio")
        @Pattern(
                regexp = "^(CEDULA_CIUDADANIA|CEDULA_EXTRANJERIA|PPT|PEP|PASAPORTE)$",
                message = "Tipo de documento inválido. Opciones permitidas: CEDULA_CIUDADANIA, CEDULA_EXTRANJERIA, PPT, PEP, PASAPORTE"
        )
        String tipoDocumento,

        @NotBlank(message = "El documento de identidad es obligatorio")
        @Pattern(
                regexp = "^(\\d{6}|\\d{8}|\\d{10})$",
                message = "El número de documento debe tener 6 u 8 dígitos (para cédula de ciudadanía) o 10 dígitos (para extranjería, PPT, PEP o pasaporte)"
        )
        String documentoIdentidad,

        String fotoPerfilURL
) {}