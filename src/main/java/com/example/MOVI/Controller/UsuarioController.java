
package com.example.MOVI.Controller;

import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Service.serviceIMP.usuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final usuarioService.UsuarioService usuarioService;


    @PatchMapping(value = "/{id}/foto-perfil", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PASAJERO')")
    public ResponseEntity<String> actualizarFotoPerfil(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) {

        String urlImagen = usuarioService.actualizarFotoPerfil(id, file);
        return ResponseEntity.ok(urlImagen);
    }

    // GET /api/usuarios -> Listar todos los usuarios
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<Usuario>> listarUsuario() {
        return ResponseEntity.ok(usuarioService.listarUsuario());
    }

    // GET /api/usuarios/{id} -> Obtener usuario por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    // GET /api/usuarios/correo/{correoElectronico}
    // Obtener usuario por correo electrónico
    @GetMapping("/correo/{correoElectronico}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Usuario> obtenerPorCorreo(
            @PathVariable String correoElectronico) {

        return ResponseEntity.ok(
                usuarioService.obtenerPorCorreo(correoElectronico)
        );
    }

    // GET /api/usuarios/documento/{numeroDocumento}
    // Obtener usuario por número de documento
    @GetMapping("/documento/{numeroDocumento}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Usuario> obtenerPorNumeroDocumento(
            @PathVariable String numeroDocumento) {

        return ResponseEntity.ok(
                usuarioService.obtenerPorNumeroDocumento(numeroDocumento)
        );
    }


    // DELETE /api/usuarios/{id} -> Eliminar usuario
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado correctamente"));
    }
}

