package com.example.MOVI.DTO.ResponseDTO;



public record OAuth2Resultado(
        boolean requiereVerificacion,
        String correoElectronico,
        authResponse autenticacion
) {

    public static OAuth2Resultado autenticado(
            String correo,
            authResponse respuesta
    ) {
        return new OAuth2Resultado(
                false,
                correo,
                respuesta
        );
    }

    public static OAuth2Resultado pendiente(
            String correo
    ) {
        return new OAuth2Resultado(
                true,
                correo,
                null
        );
    }
}

