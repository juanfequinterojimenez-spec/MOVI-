package com.example.MOVI.Config;


import com.example.MOVI.Enum.Rol;

public record UsuarioAutenticado(Long idUsuario, String correoElectronico, Rol rol) {
}
