
package com.example.MOVI.Controller;

import com.example.MOVI.Service.serviceIMP.AdminRecompensaAprobacionService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/movil/recompensas")
@RequiredArgsConstructor
public class AdminRecompensaAprobacionController {

    private final AdminRecompensaAprobacionService service;

    @PatchMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> aprobar(
            @PathVariable Long id
    ) {
        service.aprobar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> desactivar(
            @PathVariable Long id
    ) {
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
