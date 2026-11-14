package com.example.MOVI.DTO.ResponseDTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RedencionPuntosDTO(
        @NotNull(message = "El ID del usuario pasajero es obligatorio")
        Long idUsuarioPasajero,

        @NotNull(message = "El identificador de la recompensa es obligatorio")
        Long idBeneficio,

        @NotNull(message = "La cantidad de puntos a redimir es obligatoria")
        @Min(value = 1, message = "Debe redimir al menos 1 punto")
        Integer puntosARedimir
) {}
