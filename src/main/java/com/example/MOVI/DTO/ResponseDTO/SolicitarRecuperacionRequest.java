package com.example.MOVI.DTO.ResponseDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
public record SolicitarRecuperacionRequest(@NotBlank @Email String correoElectronico) {}
