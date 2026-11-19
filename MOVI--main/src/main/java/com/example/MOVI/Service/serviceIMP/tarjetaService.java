package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.DTO.ResponseDTO.tarjetaDTO;
import com.example.MOVI.DTO.ResponseDTO.tarjetaResponseDTO;
import com.example.MOVI.Entity.Tarjeta;
import com.example.MOVI.Enum.estadoTarjeta;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface tarjetaService {
    tarjetaResponseDTO vincular(String correoPasajero, tarjetaDTO dto);
    Tarjeta obtenerPorId(Long id);
    Tarjeta obtenerPorUsuario(Long usuarioId);
    List<Tarjeta> listar();
    Tarjeta cambiarEstado(Long id, estadoTarjeta nuevoEstado);
    Tarjeta obtenerPorUsuarioYEstado(Long usuarioId, estadoTarjeta estado);
    void eliminar(Long id);
    tarjetaResponseDTO registrarTarjetaConFoto(tarjetaDTO dto, MultipartFile fotoEvidencia);
    void bloquearTarjetaActual(String correoPasajero);
    tarjetaResponseDTO obtenerTarjetaActiva(String correoPasajero);
}

