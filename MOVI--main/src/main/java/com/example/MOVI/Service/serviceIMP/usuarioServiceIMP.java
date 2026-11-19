package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Exception.BusinessRuleException;
import com.example.MOVI.Exception.DuplicateResourceException;
import com.example.MOVI.Exception.ResourceNotFoundException;
import com.example.MOVI.Repository.usuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class usuarioServiceIMP implements usuarioService.UsuarioService {

    private final usuarioRepository usuarioRepository;

    // Listar todos los usuarios
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarUsuario() {
        return usuarioRepository.findAll();
    }

    // Obtener usuario por ID
    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long id) {

        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id " + id
                ));
    }

    // Obtener usuario por correo electrónico
    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerPorCorreo(String correoElectronico) {

        if (correoElectronico == null || correoElectronico.isBlank()) {
            throw new BusinessRuleException(
                    "El correo electrónico es obligatorio"
            );
        }

        return usuarioRepository.findByEmail(correoElectronico)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con correo " + correoElectronico
                ));
    }

    // Obtener usuario por número de documento
    @Override
    @Transactional(readOnly = true)
    public Usuario obtenerPorNumeroDocumento(String numeroDocumento) {

        if (numeroDocumento == null || numeroDocumento.isBlank()) {
            throw new BusinessRuleException(
                    "El número de documento es obligatorio"
            );
        }

        return usuarioRepository.findByDocumento(numeroDocumento)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con documento " + numeroDocumento
                ));
    }

    // Eliminar usuario
    @Override
    @Transactional
    public void eliminar(Long id) {

        Usuario usuario = obtenerPorId(id);

        usuarioRepository.delete(usuario);
    }

    @Override
    public String actualizarFotoPerfil(Long idUsuario, MultipartFile file) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + idUsuario));
        String urlImagen = "https://tu-proyecto.supabase.co/storage/v1/object/public/perfiles/" + file.getOriginalFilename();
        return urlImagen;
    }
}