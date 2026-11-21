package com.example.MOVI.DTO.ResponseDTO;

public record OfertaResponseDTO(
        Long idRecompensa,
        String titulo,
        String descripcionRecompensa,
        Integer stockDisponible,
        Integer costoPuntos,
        String imagenUrl,
        Boolean activa,
        AliadoResumenDTO aliado
) {
    public record AliadoResumenDTO(
            String nombre
    ) {}
}
