
package com.example.MOVI.DTO.ResponseDTO;

public record ElegibilidadResponse(
        String tipoCuenta,
        String institucion,
        boolean correoVerificado,
        boolean dominioReconocido,
        boolean beneficioEstudiantil,
        boolean beneficioEmpresarial
) {}

