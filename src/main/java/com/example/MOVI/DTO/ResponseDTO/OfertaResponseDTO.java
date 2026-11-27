
package com.example.MOVI.DTO.ResponseDTO;

public record OfertaResponseDTO(

        Long idRecompensa,

        String titulo,

        String descripcionRecompensa,

        Integer stockDisponible,

        Integer costoPuntos,

        // Imagen del cupón o producto
        String imagenUrl,

        Boolean activa,

        // Información de la marca
        AliadoResumenDTO aliado

) {

    public record AliadoResumenDTO(

            String nombre,

            // Logo cargado por el aliado
            String logoUrl,
            String categoria

    ) {}
}

