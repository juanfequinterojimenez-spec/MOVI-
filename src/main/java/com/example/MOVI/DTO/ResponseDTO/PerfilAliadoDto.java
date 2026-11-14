package com.example.MOVI.DTO.ResponseDTO;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PerfilAliadoDto(

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Debe ingresar un formato de correo válido")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        @NotBlank(message = "El nombre de la marca es obligatorio")
        String nombreMarca,

        @NotBlank(message = "La categoría es obligatoria (Ej: Restaurante, Cine, Ropa)")
        String categoria
) {}
