package com.example.MOVI.DTO.ResponseDTO;

import com.example.MOVI.Entity.Aliado;

public record AliadoResponseDTO(
        Long id,
        String nombreAliado,
        String categoria,
        String correoContacto,
        String logoUrl,
        String token // Campo limpio en el record
) {

    public static AliadoResponseDTO desde(Aliado aliado, String token) {
        return new AliadoResponseDTO(
                aliado.getId(),
                aliado.getNombreAliado(),
                aliado.getCategoriaAliado() != null ? aliado.getCategoriaAliado().name() : null,
                aliado.getCorreoContacto(),
                aliado.getLogoUrl(),
                token // Se pasa la variable recibida en el método
        );
    }
}
