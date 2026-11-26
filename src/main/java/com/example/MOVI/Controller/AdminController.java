
package com.example.MOVI.Controller;

import com.example.MOVI.DTO.ResponseDTO.AdminEstadisticasDTO;
import com.example.MOVI.DTO.ResponseDTO.AdminUsuarioDTO;
import com.example.MOVI.Service.serviceIMP.adminService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminController {

    private final adminService adminService;

    // ==========================================
    // ESTADÍSTICAS ADMINISTRATIVAS
    // ==========================================

    @GetMapping("/estadisticas")
    public ResponseEntity<AdminEstadisticasDTO> obtenerEstadisticas() {

        return ResponseEntity.ok(
                adminService.obtenerEstadisticas()
        );
    }

    // ==========================================
    // CONSULTAR USUARIOS
    // ==========================================

    @GetMapping("/usuarios")
    public ResponseEntity<List<AdminUsuarioDTO>> listarUsuarios() {

        return ResponseEntity.ok(
                adminService.listarUsuarios()
        );
    }

    // ==========================================
    // BLOQUEAR O DESBLOQUEAR USUARIO
    // ==========================================

    @PutMapping("/usuarios/{id}/toggle-bloqueo")
    public ResponseEntity<Void> toggleBloqueoUsuario(
            @PathVariable Long id
    ) {

        adminService.toggleBloqueoUsuario(id);

        return ResponseEntity.ok().build();
    }
}
