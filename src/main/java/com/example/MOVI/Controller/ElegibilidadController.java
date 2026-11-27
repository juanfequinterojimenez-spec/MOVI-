
package com.example.MOVI.Controller;

import com.example.MOVI.DTO.ResponseDTO.ElegibilidadResponse;
import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Enum.Rol;
import com.example.MOVI.Repository.usuarioRepository;
import com.example.MOVI.Service.serviceIMP.ElegibilidadService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/beneficios")
@RequiredArgsConstructor
public class ElegibilidadController {

    private final ElegibilidadService elegibilidadService;
    private final usuarioRepository usuarioRepository;

    @GetMapping("/mi-elegibilidad")
    public ResponseEntity<ElegibilidadResponse> miElegibilidad(
            Authentication authentication
    ) {

        Usuario usuario = usuarioRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado"
                        )
                );

        if (usuario.getRol() != Rol.PASAJERO ||
                !Boolean.TRUE.equals(usuario.getActivo())) {
            throw new AccessDeniedException(
                    "No autorizado para beneficios de pasajeros"
            );
        }

        return ResponseEntity.ok(
                elegibilidadService.evaluar(usuario)
        );
    }
}

