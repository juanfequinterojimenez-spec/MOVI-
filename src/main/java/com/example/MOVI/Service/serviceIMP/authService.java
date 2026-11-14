package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.DTO.ResponseDTO.authResponse;
import com.example.MOVI.DTO.ResponseDTO.loginDTO;
import com.example.MOVI.DTO.ResponseDTO.registerRequest;
import com.example.MOVI.DTO.ResponseDTO.usuarioResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface authService {

        // Registro tradicional
        authResponse registrar(registerRequest request, MultipartFile fotoDePerfil);

        // Inicio de sesión tradicional
        authResponse login(loginDTO request);

        // Inicio de sesión con Google y Microsoft
        authResponse loginOAuth2(
                String proveedor,
                String subject,
                String email,
                String nombre
        );

        // Consultar perfil
        usuarioResponseDTO perfil(Long usuarioId);



}

