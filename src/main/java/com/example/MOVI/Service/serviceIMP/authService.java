package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.DTO.ResponseDTO.RegistroPendienteResponse;
import com.example.MOVI.DTO.ResponseDTO.authResponse;
import com.example.MOVI.DTO.ResponseDTO.loginDTO;
import com.example.MOVI.DTO.ResponseDTO.registerRequest;
import com.example.MOVI.DTO.ResponseDTO.usuarioResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface authService {

        RegistroPendienteResponse registrar(
                registerRequest request,
                MultipartFile fotoDePerfil
        );

        authResponse login(loginDTO request);

        authResponse loginOAuth2(
                String proveedor,
                String subject,
                String email,
                String nombre,
                boolean correoVerificado
        );

        usuarioResponseDTO perfil(Long usuarioId);

        usuarioResponseDTO actualizarFotoPerfil(
                Long usuarioId,
                MultipartFile foto
        );
}
