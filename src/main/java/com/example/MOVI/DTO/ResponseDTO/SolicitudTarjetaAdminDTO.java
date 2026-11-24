
package com.example.MOVI.DTO.ResponseDTO;

import com.example.MOVI.Entity.Tarjeta;
import java.time.LocalDateTime;

public record SolicitudTarjetaAdminDTO(
        Long idTarjeta,
        String serialEnmascarado,
        String estadoOperativo,
        String estadoVerificacion,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaVerificacion,
        String observacionesRevision
) {

    public static SolicitudTarjetaAdminDTO desde(
            Tarjeta tarjeta
    ) {

        String serial = tarjeta.getNumeroSerie();

        String enmascarado =
                serial == null ? "Sin serial" :
                        "•••• " + serial.substring(
                                Math.max(0, serial.length() - 4)
                        );

        return new SolicitudTarjetaAdminDTO(
                tarjeta.getIdTarjeta(),
                enmascarado,
                tarjeta.getEstado().name(),
                tarjeta.getEstadoVerificacion().name(),
                tarjeta.getFechaSolicitud(),
                tarjeta.getFechaVerificacion(),
                tarjeta.getObservacionesRevision()
        );
    }
}

