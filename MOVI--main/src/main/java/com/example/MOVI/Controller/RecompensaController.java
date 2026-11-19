package com.example.MOVI.Controller;

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


    // LISTAR RECOMPENSAS POR ALIADO
    @GetMapping("/aliado/{idAliado}")
    @PreAuthorize("hasRole('PASAJERO') or hasRole('ALIADO')")
    public ResponseEntity<List<Recompensa>> listarPorAliado(
            @PathVariable Long idAliado) {

        List<Recompensa> recompensas =
                recompensaService.listarPorAliado(idAliado);

        return ResponseEntity.ok(recompensas);
    }

    @GetMapping("/ofertas")
    @PreAuthorize("hasRole('PASAJERO')")
    public ResponseEntity<List<Recompensa>> verOfertas(
            @RequestParam(name = "categoria", required = false) String categoria) {

        List<Recompensa> catalogo = recompensaService.obtenerCatalogo(categoria);
        return ResponseEntity.ok(catalogo);
    }

}