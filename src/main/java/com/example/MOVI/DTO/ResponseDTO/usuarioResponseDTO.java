package com.example.MOVI.DTO.ResponseDTO;

import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Enum.Rol;

public record usuarioResponseDTO(
    Long id,
    String nombreCompleto,
    String email,
    Integer puntos,
    String fotoPerfilURL,
    Rol rol,
    String tipoDocumento,
    String documentoIdentidad
) {
    public static usuarioResponseDTO desde(Usuario usuario) {
        return new usuarioResponseDTO(
            usuario.getId(), usuario.getNombreCompleto(), usuario.getEmail(),
            usuario.getPuntos(), usuario.getFotoPerfilURL(), usuario.getRol(),
            usuario.getTipoDocumento(), usuario.getDocumento()
        );
    }
}
