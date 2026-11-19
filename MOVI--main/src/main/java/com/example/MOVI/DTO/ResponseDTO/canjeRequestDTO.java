package com.example.MOVI.DTO.ResponseDTO;


import jakarta.validation.constraints.NotNull;

public record canjeRequestDTO(
        @NotNull(message = "El ID del usuario es obligatorio")
        Long idUsuario,

        @NotNull(message = "El ID de la recompensa es obligatorio")
        Long idRecompensa
) {}
