package com.example.MOVI.DTO.ResponseDTO;

import com.example.MOVI.Entity.Aliado;

// Nombres de campos alineados 1:1 con SolicitudAliadoUi en el cliente Android
// (Data/Model/AdminModel.kt): id, negocio, correo, categoria, direccion.
public record AdminAliadoDTO(
        String id,
        String negocio,
        String correo,
        String categoria,
        String direccion
) {
    public static AdminAliadoDTO desde(Aliado aliado) {
        return new AdminAliadoDTO(
                String.valueOf(aliado.getId()),
                aliado.getNombreAliado(),
                aliado.getCorreoContacto(),
                aliado.getCategoriaAliado() != null ? aliado.getCategoriaAliado().name() : null,
                // La entidad Aliado no tiene una dirección física registrada todavía.
                ""
        );
    }
}
