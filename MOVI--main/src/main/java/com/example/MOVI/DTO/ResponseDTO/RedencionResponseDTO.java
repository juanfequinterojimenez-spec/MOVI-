package com.example.MOVI.DTO.ResponseDTO;


import java.time.LocalDateTime;

public record RedencionResponseDTO(
        Long idTransaccion,
        Long idUsuarioPasajero,
        Integer puntosRedimidos,
        Integer saldoPuntosRestante,
        LocalDateTime fechaHoraRedencion,
        String mensaje
) {}
