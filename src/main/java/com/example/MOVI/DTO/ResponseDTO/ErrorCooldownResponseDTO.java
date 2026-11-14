package com.example.MOVI.DTO.ResponseDTO;

import java.time.LocalDateTime;

public record ErrorCooldownResponseDTO(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        long minutosRestantes,
        long segundosRestantes,
        String path
) {}
