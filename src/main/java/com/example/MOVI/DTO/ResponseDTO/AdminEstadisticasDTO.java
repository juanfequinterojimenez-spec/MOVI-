package com.example.MOVI.DTO.ResponseDTO;

// Nombres de campos alineados 1:1 con AdminEstadisticasUi en el cliente Android
// (Data/Model/AdminModel.kt), para que Gson los mapee sin configuración extra.
public record AdminEstadisticasDTO(
        String viajesSimulados,
        String puntosEmitidos,
        String canjesRealizados,
        Integer solicitudesPendientes
) {
}
