package com.example.MOVI.Service.serviceIMP;


import com.example.MOVI.DTO.ResponseDTO.AliadoRegistroDTO;
import com.example.MOVI.DTO.ResponseDTO.AliadoResponseDTO;
import com.example.MOVI.DTO.ResponseDTO.PerfilAliadoDto;
import com.example.MOVI.DTO.ResponseDTO.recompensaRequestDTO;
import com.example.MOVI.Entity.Recompensa;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface aliadoService {
    List<Recompensa> obtenerOfertasPorAliado(String correoAliado);
    void actualizarPerfil(String correoAliado, PerfilAliadoDto dto, MultipartFile logo);
    Recompensa publicarOferta(String correoAliado, recompensaRequestDTO dto, MultipartFile imagen);
    AliadoResponseDTO registrarAliado(AliadoRegistroDTO dto, MultipartFile logo);
}
