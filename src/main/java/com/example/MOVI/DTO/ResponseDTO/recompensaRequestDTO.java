package com.example.MOVI.DTO.ResponseDTO;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record recompensaRequestDTO(
        @NotBlank(message = "El título de la oferta es obligatorio")
        String titulo,

        @NotBlank(message = "La descripción es obligatoria")
        String descripcion,

        @NotNull(message = "El costo en puntos es obligatorio")
        @Min(value = 1, message = "El costo debe ser al menos 1 punto")
        Integer costoPuntos,

        @NotNull(message = "Debes indicar cuántos cupones hay disponibles")
        @Min(value = 1, message = "Debe haber al menos 1 cupón")
        Integer stockDisponible
) {}
