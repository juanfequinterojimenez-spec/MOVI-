package com.example.MOVI.DTO.ResponseDTO;

import lombok.Builder;
import lombok.Data;


@Builder
public record JwtResponseDTO(
        String token,
        String tipoToken,
        Long idUsuario,
        String nombreCompleto,
        String correoElectronico,
        String rol,
        Integer puntosActuales,
        String urlFotoPerfil
) {
    public JwtResponseDTO{
        if (tipoToken == null) {
            tipoToken = "Bearer";
        }
    }
}
