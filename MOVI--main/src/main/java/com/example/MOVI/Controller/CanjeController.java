package com.example.MOVI.Controller;


import com.example.MOVI.DTO.ResponseDTO.CanjeResponseDto;
import com.example.MOVI.DTO.ResponseDTO.QrTemporalResponseDTO;
import com.example.MOVI.DTO.ResponseDTO.RedencionPuntosDTO;
import com.example.MOVI.DTO.ResponseDTO.RedencionResponseDTO;
import com.example.MOVI.Service.serviceIMP.canjeService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/canjes")
@AllArgsConstructor
public class CanjeController {

    private final canjeService canjeService;

    // 1. Pasajero canjea puntos por una recompensa
    @PostMapping("/{recompensaId}/canjear")
    @PreAuthorize("hasRole('PASAJERO')")
    public ResponseEntity<CanjeResponseDto> canjearOferta(
            @PathVariable Long recompensaId,
            Authentication authentication) {

        CanjeResponseDto response = canjeService.procesarCanje(authentication.getName(), recompensaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. Pasajero consulta su billetera de cupones
    @GetMapping("/billetera")
    @PreAuthorize("hasRole('PASAJERO')")
    public ResponseEntity<Page<CanjeResponseDto>> obtenerBilletera(
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(canjeService.obtenerBilleteraPorEstado(authentication.getName(), estado, pageable));
    }

    // 3. Pasajero descarga el comprobante PDF con QR de un canje suyo
    @GetMapping(value = "/{canjeId}/comprobante-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasRole('PASAJERO')")
    public ResponseEntity<byte[]> descargarComprobantePdf(
            @PathVariable Long canjeId,
            Authentication authentication) {

        byte[] pdfBytes = canjeService.generarPdfDeCanje(canjeId, authentication.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=comprobante_movi_" + canjeId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }

    // 4. Pasajero genera QR temporal de 2 minutos en caja
    @GetMapping("/qr-temporal")
    @PreAuthorize("hasRole('PASAJERO')")
    public ResponseEntity<QrTemporalResponseDTO> generarQrTemporal(Authentication authentication) {
        return ResponseEntity.ok(canjeService.generarQrTemporal(authentication.getName()));
    }

    // 5. Aliado valida cupón mediante código comprobante (o lectura de QR)
    @PatchMapping("/validar/{codigoComprobante}")
    @PreAuthorize("hasRole('ALIADO')")
    public ResponseEntity<CanjeResponseDto> validarCupon(
            @PathVariable String codigoComprobante,
            Authentication authentication) {

        return ResponseEntity.ok(canjeService.validarCupon(authentication.getName(), codigoComprobante));
    }

    // 6. Aliado redime puntos directamente en tienda
    @PostMapping("/redimir")
    @PreAuthorize("hasRole('ALIADO')")
    public ResponseEntity<RedencionResponseDTO> redimirPuntos(
            @RequestBody @Valid RedencionPuntosDTO request,
            Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(canjeService.redimirPuntos(request, authentication.getName()));
    }
}

