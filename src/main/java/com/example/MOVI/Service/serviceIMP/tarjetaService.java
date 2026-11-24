package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.DTO.ResponseDTO.tarjetaDTO;
import com.example.MOVI.DTO.ResponseDTO.tarjetaResponseDTO;
import com.example.MOVI.Entity.Tarjeta;
import com.example.MOVI.Enum.estadoTarjeta;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface tarjetaService {

    // Vinculación sin fotografía
    tarjetaResponseDTO vincular(
            String correoPasajero,
            tarjetaDTO dto
    );

    // Registro con evidencia fotográfica
    tarjetaResponseDTO registrarTarjetaConFoto(
            String correoPasajero,
            tarjetaDTO dto,
            MultipartFile fotoEvidencia
    );

    // Consultar tarjeta por ID
    Tarjeta obtenerPorId(Long id);

    // Consultar tarjeta de un usuario
    Tarjeta obtenerPorUsuario(Long usuarioId);

    // Listar tarjetas
    List<Tarjeta> listar();

    // Cambiar estado operativo
    Tarjeta cambiarEstado(
            Long id,
            estadoTarjeta nuevoEstado
    );

    // Consultar por usuario y estado
    Tarjeta obtenerPorUsuarioYEstado(
            Long usuarioId,
            estadoTarjeta estado
    );

    // Eliminar tarjeta
    void eliminar(Long id);

    // Bloquear tarjeta actual
    void bloquearTarjetaActual(
            String correoPasajero
    );

    // Consultar tarjeta activa
    tarjetaResponseDTO obtenerTarjetaActiva(
            String correoPasajero
    );
}

