
package com.example.MOVI.Controller;


import com.example.MOVI.DTO.ResponseDTO.ViajeHistorialResponseDTO;
import com.example.MOVI.DTO.ResponseDTO.ViajeResumenResponseDTO;
import com.example.MOVI.DTO.ResponseDTO.viajeResponseDTO;
import com.example.MOVI.DTO.ResponseDTO.viajeSimulacionDTO;
import com.example.MOVI.Entity.Viaje;
import com.example.MOVI.Service.serviceIMP.viajeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/viajes")
@RequiredArgsConstructor
public class ViajeController {

    private final viajeService viajeService;

    // POST /api/viajes -> Registrar/Simular un viaje (Exclusivo para el Pasajero autenticado)
    @PostMapping
    @PreAuthorize("hasRole('PASAJERO')")
    public ResponseEntity<viajeResponseDTO> registrarViaje(
            @Valid @RequestBody viajeSimulacionDTO request) {

        viajeResponseDTO respuesta = viajeService.registrarViaje(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // Obtener un viaje específico por su ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ALIADO') or hasRole('PASAJERO')")
    public ResponseEntity<Viaje> obtenerPorId(@PathVariable Long id) {
        Viaje viaje = viajeService.obtenerPorId(id);
        return ResponseEntity.ok(viaje);
    }

    // Listar todos los viajes de un usuario ordenados por fecha
    @GetMapping("/mis-viajes")
    @PreAuthorize("hasRole('PASAJERO')")
    public ResponseEntity<List<ViajeHistorialResponseDTO>> listarMiHistorial(Authentication authentication) {
        String correoPasajero = authentication.getName();
        return ResponseEntity.ok(viajeService.listarHistorialPorCorreo(correoPasajero));
    }

    // 2. Endpoint para el Administrador o Soporte (Usa el ID por URL)
    @GetMapping("/usuario/{usuarioId}/historial")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ALIADO')")
    public ResponseEntity<List<ViajeHistorialResponseDTO>> listarHistorialPorUsuario(
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(viajeService.listarHistorialPorUsuario(usuarioId));
    }

    @GetMapping("/resumen-diario")
    @PreAuthorize("hasRole('PASAJERO')")
    public ResponseEntity<ViajeResumenResponseDTO> obtenerResumenDiario(Authentication authentication) {
        String correoPasajero = authentication.getName();
        ViajeResumenResponseDTO resumen = viajeService.obtenerResumenDiario(correoPasajero);
        return ResponseEntity.ok(resumen);
    }
}


