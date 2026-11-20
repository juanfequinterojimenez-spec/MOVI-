package com.example.MOVI.Controller;

import com.example.MOVI.Entity.Recompensa;
import com.example.MOVI.Exception.ResourceNotFoundException;
import com.example.MOVI.Repository.recompensaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/admin/productos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AprobacionProductoController {
    private final recompensaRepository recompensas;

    @PatchMapping("/{id}/validar")
    @Transactional
    public void validar(@PathVariable Long id) {
        Recompensa r = recompensas.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        r.setActiva(true);
        recompensas.save(r);
    }
}
