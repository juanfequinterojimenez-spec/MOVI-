package com.example.MOVI.Service.serviceIMP;

import com.example.MOVI.DTO.ResponseDTO.*;
import com.example.MOVI.Enum.EstadoCanje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface canjeService {
    CanjeResponseDto procesarCanje(String correoPasajero, Long recompensaId);
    CanjeResponseDto validarCupon( String correoAliado, String correoComprobante);
    QrTemporalResponseDTO generarQrTemporal(String correoPasajero);
    RedencionResponseDTO redimirPuntos(RedencionPuntosDTO request, String correoAliado);
    Page<CanjeResponseDto> obtenerBilleteraPorEstado(String correoPasajero, String estado, Pageable pageable);
    byte[] generarPdfDeCanje(Long canjeId, String correoPasajero);
}
