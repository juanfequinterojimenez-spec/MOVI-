package com.example.MOVI.Service.serviceIMP;


import com.example.MOVI.DTO.ResponseDTO.ViajeHistorialResponseDTO;
import com.example.MOVI.DTO.ResponseDTO.ViajeResumenResponseDTO;
import com.example.MOVI.DTO.ResponseDTO.viajeResponseDTO;
import com.example.MOVI.DTO.ResponseDTO.viajeSimulacionDTO;
import com.example.MOVI.Entity.Viaje;

import java.util.List;

public interface viajeService {
    viajeResponseDTO registrarViaje(viajeSimulacionDTO request);
    Viaje obtenerPorId(Long id);
    List<ViajeHistorialResponseDTO> listarHistorialPorUsuario(Long usuarioId);
    List<ViajeHistorialResponseDTO> listarHistorialPorCorreo(String correoPasajero);
    ViajeResumenResponseDTO obtenerResumenDiario(String correoPasajero);
}
