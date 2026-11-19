package com.example.MOVI.DTO.ResponseDTO;

public record ViajeResumenResponseDTO(
        Long usuarioId,
        Integer puntosActuales,
        long viajesRealizadosHoy,
        long viajesRestantesHoy,
        int maxViajesDiarios
) {}
