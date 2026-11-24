package com.example.MOVI.DTO.ResponseDTO;

import com.example.MOVI.Entity.Tarjeta;

import java.time.LocalDateTime;

public record tarjetaResponseDTO(

        Long idTarjeta,

        String numeroSerie,

        // Estado operativo: ACTIVA, BLOQUEADA, etc.
        String estado,

        LocalDateTime fechaVinculacion,

        // Estado de personalización
        String estadoVerificacion,

        // Información sobre la revisión
        LocalDateTime fechaSolicitud,

        LocalDateTime fechaVerificacion,

        String observacionesRevision

) {

    public static tarjetaResponseDTO desde(Tarjeta tarjeta) {

        return new tarjetaResponseDTO(

                tarjeta.getIdTarjeta(),

                tarjeta.getNumeroSerie(),

                tarjeta.getEstado() != null
                        ? tarjeta.getEstado().name()
                        : null,

                tarjeta.getFechaVinculacion(),

                tarjeta.getEstadoVerificacion() != null
                        ? tarjeta.getEstadoVerificacion().name()
                        : "PENDIENTE",

                tarjeta.getFechaSolicitud(),

                tarjeta.getFechaVerificacion(),

                tarjeta.getObservacionesRevision()
        );
    }
}
