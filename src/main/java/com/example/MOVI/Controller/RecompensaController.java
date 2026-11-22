package com.example.MOVI.Controller;

import com.example.MOVI.DTO.ResponseDTO.OfertaResponseDTO;
import com.example.MOVI.Entity.Recompensa;
import com.example.MOVI.Service.serviceIMP.recompensaService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recompensas")
@AllArgsConstructor
public class RecompensaController {

    private final recompensaService recompensaService;

    // Recompensas de un aliado
    @GetMapping("/aliado/{idAliado}")
    @PreAuthorize("hasRole('PASAJERO') or hasRole('ALIADO')")
    public ResponseEntity<List<OfertaResponseDTO>> listarPorAliado(
            @PathVariable Long idAliado
    ) {
        return ResponseEntity.ok(
                recompensaService.listarPorAliado(idAliado)
        );
    }

    // Catálogo de beneficios para pasajeros
    @GetMapping("/ofertas")
    @PreAuthorize("hasRole('PASAJERO')")
    public ResponseEntity<List<OfertaResponseDTO>> verOfertas(
            @RequestParam(
                    name = "categoria",
                    required = false
            ) String categoria
    ) {
        return ResponseEntity.ok(
                recompensaService.obtenerCatalogoOfertas(categoria)
        );
    }
}