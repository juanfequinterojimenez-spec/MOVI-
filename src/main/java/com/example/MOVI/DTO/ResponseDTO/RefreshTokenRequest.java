package com.example.MOVI.DTO.ResponseDTO;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(@NotBlank String refreshToken) {}
