package com.example.MOVI.Controller;

import com.example.MOVI.DTO.ResponseDTO.*;
import com.example.MOVI.Service.serviceIMP.RecuperacionContrasenaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/recuperacion")
@RequiredArgsConstructor
public class RecuperacionContrasenaController {
    private final RecuperacionContrasenaService servicio;

    @PostMapping("/solicitar")
    public ResponseEntity<Void> solicitar(@Valid @RequestBody SolicitarRecuperacionRequest request) {
        servicio.solicitar(request);
        return ResponseEntity.accepted().cacheControl(CacheControl.noStore()).build();
    }

    @PostMapping("/confirmar")
    public ResponseEntity<Void> confirmar(@Valid @RequestBody ConfirmarRecuperacionRequest request) {
        servicio.confirmar(request);
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }
}
