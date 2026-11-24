package com.example.MOVI.DTO.ResponseDTO;

import com.example.MOVI.Enum.EstadoVerificacionTarjeta;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RevisionTarjetaDTO(

        @NotNull
        EstadoVerificacionTarjeta estado,

        @Size(max = 500)
        String observaciones

) {}
