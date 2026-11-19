package com.example.MOVI.Controller;

import com.example.MOVI.DTO.ResponseDTO.authResponse;
import com.example.MOVI.DTO.ResponseDTO.loginDTO;
import com.example.MOVI.DTO.ResponseDTO.registerRequest;
import com.example.MOVI.DTO.ResponseDTO.usuarioResponseDTO;

import com.example.MOVI.Service.serviceIMP.authService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import com.example.MOVI.Config.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final authService authService;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<authResponse> registrar(
            @Parameter(description = "Datos de registro", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart("request") @Valid registerRequest request,

            @Parameter(description = "Foto de perfil opcional")
            @RequestPart(value = "fotoPerfil", required = false) MultipartFile fotoDePerfil) {

        authResponse respuesta = authService.registrar(request, fotoDePerfil);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }


    //login tradicional
    @PostMapping("/login")
    public ResponseEntity<authResponse> login(
            @Valid @RequestBody loginDTO request) {

        authResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }


    // CONSULTAR PERFIL
    @GetMapping("/perfil")
    @PreAuthorize("hasRole('PASAJERO')")
    public ResponseEntity<usuarioResponseDTO> perfil(
            Authentication authentication) {

        UsuarioAutenticado usuario =
                (UsuarioAutenticado) authentication.getDetails();

        usuarioResponseDTO response =
                authService.perfil(usuario.idUsuario());

        return ResponseEntity.ok(response);
    }
}

