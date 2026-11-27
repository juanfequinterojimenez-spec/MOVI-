package com.example.MOVI.DTO.ResponseDTO;

import java.time.Instant;

public record SesionPersistenteResponse(
        String token,
        String refreshToken,
        Instant sesionVenceEn,
        usuarioResponseDTO usuario
) {}
