package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.Entity.Usuario;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface usuarioService {
    public interface UsuarioService {

        List<Usuario> listarUsuario();

        Usuario obtenerPorId(Long id);

        Usuario obtenerPorCorreo(String correoElectronico);

        Usuario obtenerPorNumeroDocumento(String numeroDocumento);


        void eliminar(Long id);

        String actualizarFotoPerfil(Long idUsuario, MultipartFile file);
    }
}
