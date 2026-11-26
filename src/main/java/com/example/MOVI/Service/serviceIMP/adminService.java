
package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.DTO.ResponseDTO.AdminEstadisticasDTO;
import com.example.MOVI.DTO.ResponseDTO.AdminUsuarioDTO;

import java.util.List;

public interface adminService {

    AdminEstadisticasDTO obtenerEstadisticas();

    List<AdminUsuarioDTO> listarUsuarios();

    void toggleBloqueoUsuario(Long usuarioId);
}
