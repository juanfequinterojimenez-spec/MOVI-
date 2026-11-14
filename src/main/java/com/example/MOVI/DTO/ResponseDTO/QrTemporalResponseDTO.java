package com.example.MOVI.DTO.ResponseDTO;


import java.time.LocalDateTime;

public record QrTemporalResponseDTO(
        String tokenQr,          // Cadena cifrada que la app dibuja como código QR
        long segundosValidez,    // Ej. 120 segundos (2 minutos)
        LocalDateTime expiraEn   // Hora exacta de expiración
) {}
