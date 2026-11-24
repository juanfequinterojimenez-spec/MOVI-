
package com.example.MOVI.Controller;


import com.example.MOVI.DTO.ResponseDTO.RevisionTarjetaDTO;
import com.example.MOVI.DTO.ResponseDTO.SolicitudTarjetaAdminDTO;
import com.example.MOVI.Entity.Tarjeta;
import com.example.MOVI.Enum.EstadoVerificacionTarjeta;
import com.example.MOVI.Repository.tarjetaRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/tarjetas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminTarjetaController {

    private final tarjetaRepository tarjetaRepository;

    @GetMapping("/pendientes")
    @Transactional(readOnly = true)
    public List<SolicitudTarjetaAdminDTO> pendientes() {

        return tarjetaRepository
                .findByEstadoVerificacionIn(
                        List.of(
                                EstadoVerificacionTarjeta.PENDIENTE,
                                EstadoVerificacionTarjeta.EN_REVISION
                        )
                )
                .stream()
                .map(SolicitudTarjetaAdminDTO::desde)
                .toList();
    }

    @PatchMapping("/{id}/revision")
    @Transactional
    public ResponseEntity<SolicitudTarjetaAdminDTO> revisar(
            @PathVariable Long id,
            @Valid @RequestBody RevisionTarjetaDTO request
    ) {

        Tarjeta tarjeta = tarjetaRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Solicitud de tarjeta no encontrada"
                        )
                );

        EstadoVerificacionTarjeta nuevoEstado =
                request.estado();

        // La verificación oficial solo puede provenir
        // de un proveedor institucional autorizado.
        if (nuevoEstado ==
                EstadoVerificacionTarjeta.VERIFICADA_OFICIALMENTE) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La verificación oficial no puede " +
                            "asignarse manualmente"
            );
        }

        if (nuevoEstado ==
                EstadoVerificacionTarjeta.PENDIENTE) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se permite regresar a PENDIENTE"
            );
        }

        if (tarjeta.getEstadoVerificacion() ==
                EstadoVerificacionTarjeta.VERIFICADA_OFICIALMENTE) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta tarjeta tiene una verificación oficial"
            );
        }

        tarjeta.setEstadoVerificacion(nuevoEstado);

        tarjeta.setObservacionesRevision(
                request.observaciones()
        );

        if (nuevoEstado ==
                EstadoVerificacionTarjeta.VERIFICADA_MANUALMENTE ||
                nuevoEstado ==
                        EstadoVerificacionTarjeta.RECHAZADA) {

            tarjeta.setFechaVerificacion(
                    LocalDateTime.now()
            );

        } else {
            tarjeta.setFechaVerificacion(null);
        }

        Tarjeta actualizada =
                tarjetaRepository.save(tarjeta);

        return ResponseEntity.ok(
                SolicitudTarjetaAdminDTO.desde(actualizada)
        );
    }
}

