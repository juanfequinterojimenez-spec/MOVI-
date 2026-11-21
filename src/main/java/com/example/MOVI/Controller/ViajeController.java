package com.example.MOVI.Controller;

import com.example.MOVI.DTO.ResponseDTO.*;
import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Entity.Viaje;
import com.example.MOVI.Entity.Tarjeta;
import com.example.MOVI.Enum.estadoTarjeta;
import com.example.MOVI.Exception.BusinessRuleException;
import com.example.MOVI.Exception.ResourceNotFoundException;
import com.example.MOVI.Repository.usuarioRepository;
import com.example.MOVI.Repository.tarjetaRepository;
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
    private final usuarioRepository usuarioRepository;
    private final tarjetaRepository tarjetaRepository;

    // Endpoint legado: se comprueba que el ID del body corresponde al JWT.
    @PostMapping
    @PreAuthorize("hasRole('PASAJERO')")
    public ResponseEntity<viajeResponseDTO> registrarViaje(
            @Valid @RequestBody viajeSimulacionDTO request,
            Authentication authentication) {
        Usuario usuario = usuarioAutenticado(authentication);
        if (!usuario.getId().equals(request.idUsuario())) {
            throw new BusinessRuleException("No puedes registrar viajes para otra cuenta.");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(viajeService.registrarViaje(request));
    }

    // Endpoint sencillo para Android: sin IDs proporcionados por el cliente.
    @PostMapping("/prueba")
    @PreAuthorize("hasRole('PASAJERO')")
    public ResponseEntity<viajeResponseDTO> registrarViajePrueba(
            @Valid @RequestBody viajePruebaRequest request,
            Authentication authentication) {
        Usuario usuario = usuarioAutenticado(authentication);
        Tarjeta tarjeta = tarjetaRepository
                .findByUsuario_IdAndEstado(usuario.getId(), estadoTarjeta.ACTIVA)
                .orElseThrow(() -> new BusinessRuleException(
                        "Necesitas una tarjeta activa para realizar viajes."));
        viajeSimulacionDTO datos = new viajeSimulacionDTO(
                usuario.getId(), tarjeta.getIdTarjeta(),
                request.nombreEstacion().trim(), request.tipoComponente().trim().toUpperCase());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(viajeService.registrarViaje(datos));
    }

    private Usuario usuarioAutenticado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessRuleException("Debes iniciar sesión para realizar viajes.");
        }
        return usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ALIADO') or hasRole('PASAJERO')")
    public ResponseEntity<Viaje> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(viajeService.obtenerPorId(id));
    }

    @GetMapping("/mis-viajes")
    @PreAuthorize("hasRole('PASAJERO')")
    public ResponseEntity<List<ViajeHistorialResponseDTO>> listarMiHistorial(Authentication authentication) {
        return ResponseEntity.ok(viajeService.listarHistorialPorCorreo(authentication.getName()));
    }

    @GetMapping("/usuario/{usuarioId}/historial")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ALIADO')")
    public ResponseEntity<List<ViajeHistorialResponseDTO>> listarHistorialPorUsuario(
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(viajeService.listarHistorialPorUsuario(usuarioId));
    }

    @GetMapping("/resumen-diario")
    @PreAuthorize("hasRole('PASAJERO')")
    public ResponseEntity<ViajeResumenResponseDTO> obtenerResumenDiario(Authentication authentication) {
        return ResponseEntity.ok(viajeService.obtenerResumenDiario(authentication.getName()));
    }
}
