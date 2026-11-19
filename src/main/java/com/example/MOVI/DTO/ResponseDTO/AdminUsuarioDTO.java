package com.example.MOVI.DTO.ResponseDTO;

import com.example.MOVI.Entity.Usuario;

// Nombres de campos alineados 1:1 con UsuarioAdminUi en el cliente Android
// (Data/Model/AdminModel.kt): id, nombre, estado, activo.
public record AdminUsuarioDTO(
        String id,
        String nombre,
        String estado,
        Boolean activo
) {
    public static AdminUsuarioDTO desde(Usuario usuario) {
        boolean activo = Boolean.TRUE.equals(usuario.getActivo());

        return new AdminUsuarioDTO(
                String.valueOf(usuario.getId()),
                usuario.getNombreCompleto(),
                activo ? "Activo" : "Bloqueado: actividad sospechosa",
                activo
        );
    }
}
