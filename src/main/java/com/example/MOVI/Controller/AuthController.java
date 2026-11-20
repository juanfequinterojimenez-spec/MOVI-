package com.example.MOVI.Controller;

import com.example.MOVI.Config.UsuarioAutenticado;
import com.example.MOVI.DTO.ResponseDTO.authResponse;
import com.example.MOVI.DTO.ResponseDTO.loginDTO;
import com.example.MOVI.DTO.ResponseDTO.registerRequest;
import com.example.MOVI.DTO.ResponseDTO.usuarioResponseDTO;
import com.example.MOVI.Service.serviceIMP.authService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final authService authService;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<authResponse> registrar(
            @Parameter(description = "Datos de registro", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart("request") @Valid registerRequest request,
            @RequestPart(value = "fotoPerfil", required = false) MultipartFile fotoDePerfil) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request, fotoDePerfil));
    }

    @PostMapping("/login")
    public ResponseEntity<authResponse> login(@Valid @RequestBody loginDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/perfil")
    @PreAuthorize("hasAnyRole('PASAJERO','ALIADO','ADMINISTRADOR')")
    public ResponseEntity<usuarioResponseDTO> perfil(Authentication authentication) {
        return ResponseEntity.ok(authService.perfil(usuarioActual(authentication)));
    }

    @PatchMapping(value = "/perfil/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PASAJERO')")
    public ResponseEntity<usuarioResponseDTO> actualizarFoto(
            Authentication authentication,
            @RequestPart("fotoPerfil") MultipartFile fotoPerfil) {
        return ResponseEntity.ok(authService.actualizarFotoPerfil(usuarioActual(authentication), fotoPerfil));
    }

    private Long usuarioActual(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof UsuarioAutenticado usuario)) {
            throw new org.springframework.security.access.AccessDeniedException("No autenticado");
        }
        return usuario.idUsuario();
    }
}
