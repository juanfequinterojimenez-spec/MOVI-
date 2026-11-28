package com.example.MOVI.DTO.ResponseDTO;
import jakarta.validation.constraints.*;
public record ConfirmarRecuperacionRequest(
    @NotBlank @Email String correoElectronico,
    @NotBlank @Pattern(regexp="\\d{6}") String codigo,
    @NotBlank @Size(min=10, max=128) String nuevaContrasena
) {}
