package com.example.MOVI.Controller;

import com.example.MOVI.Config.UsuarioAutenticado;
import com.example.MOVI.DTO.ResponseDTO.RefreshTokenRequest;
import com.example.MOVI.DTO.ResponseDTO.SesionPersistenteResponse;
import com.example.MOVI.Service.serviceIMP.SesionPersistenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/sesion")
@RequiredArgsConstructor
public class SesionPersistenteController {
    private final SesionPersistenteService servicio;

    // Solo se llama después de login/verificación exitosa con JWT válido.
    @PostMapping("/activar")
    public ResponseEntity<SesionPersistenteResponse> activar(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof UsuarioAutenticado u)) {
            throw new AccessDeniedException("No autenticado");
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(servicio.activar(u.idUsuario()));
    }

    @PostMapping("/renovar")
    public ResponseEntity<SesionPersistenteResponse> renovar(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(servicio.renovar(request.refreshToken()));
    }

    @PostMapping("/revocar")
    public ResponseEntity<Void> revocar(@Valid @RequestBody RefreshTokenRequest request) {
        servicio.revocar(request.refreshToken());
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }
}
