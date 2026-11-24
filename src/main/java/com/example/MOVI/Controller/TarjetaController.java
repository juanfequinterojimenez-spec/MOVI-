package com.example.MOVI.Controller;


import com.example.MOVI.DTO.ResponseDTO.tarjetaDTO;
import com.example.MOVI.DTO.ResponseDTO.tarjetaResponseDTO;
import com.example.MOVI.Entity.Tarjeta;
import com.example.MOVI.Enum.estadoTarjeta;
import com.example.MOVI.Service.serviceIMP.tarjetaService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

import static org.springframework.security.authorization.AuthorityReactiveAuthorizationManager.hasRole;

@RestController
@RequestMapping("/api/tarjetas")
@AllArgsConstructor
public class TarjetaController {

    private final tarjetaService tarjetaService;


    //vincular tarjeta
    @PostMapping(value = "/registrar-con-foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('PASAJERO')")
    public ResponseEntity<tarjetaResponseDTO>
    registrarTarjetaConFoto(

            @RequestPart("dto")
            @Valid tarjetaDTO dto,

            @RequestPart("foto")
            MultipartFile fotoEvidencia,

            Authentication authentication
    ) {

        String correoPasajero = authentication.getName();

        tarjetaResponseDTO respuesta =
                tarjetaService.registrarTarjetaConFoto(
                        correoPasajero,
                        dto,
                        fotoEvidencia
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }



    //obtener tarjeta id
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Tarjeta> obtenerPorId(
            @PathVariable Long id) {

        Tarjeta tarjeta = tarjetaService.obtenerPorId(id);

        return ResponseEntity.ok(tarjeta);
    }

    // obtener tarjeta por usuario
    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Tarjeta> obtenerPorUsuario(
            @PathVariable Long usuarioId) {

        Tarjeta tarjeta =
                tarjetaService.obtenerPorUsuario(usuarioId);

        return ResponseEntity.ok(tarjeta);
    }


    // lista tarjetas
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('ALIADO')")
    public ResponseEntity<List<Tarjeta>> listar() {

        List<Tarjeta> tarjetas = tarjetaService.listar();

        return ResponseEntity.ok(tarjetas);
    }


    // lista estado de la tarjeta por id
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Tarjeta> cambiarEstado(
            @PathVariable Long id,
            @RequestParam estadoTarjeta nuevoEstado) {

        Tarjeta tarjeta = tarjetaService.cambiarEstado(
                id,
                nuevoEstado
        );

        return ResponseEntity.ok(tarjeta);
    }
    // estado de la tarjeta del usuario id
    @GetMapping("/usuario/{usuarioId}/estado/{estado}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Tarjeta> obtenerPorUsuarioYEstado(
            @PathVariable Long usuarioId,
            @PathVariable estadoTarjeta estado) {

        Tarjeta tarjeta =
                tarjetaService.obtenerPorUsuarioYEstado(
                        usuarioId,
                        estado
                );

        return ResponseEntity.ok(tarjeta);
    }


    // eliminar tarjeta
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        tarjetaService.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    // 1. Bloquear / "Eliminar" tarjeta perdida
    @PatchMapping("/bloquear")
    @PreAuthorize("hasRole('PASAJERO')")
    public ResponseEntity<String> bloquearTarjeta(Authentication authentication) {

        // Obtenemos el correo del pasajero autenticado mediante el token JWT
        String correoPasajero = authentication.getName();

        // Invocamos el servicio que busca la tarjeta ACTIVA y cambia su estado a BLOQUEADA
        tarjetaService.bloquearTarjetaActual(correoPasajero);

        return ResponseEntity.ok("Tarjeta bloqueada exitosamente. Ya puedes proceder a vincular tu nueva tarjeta.");
    }

    @GetMapping("/activa")
    @PreAuthorize("hasRole('PASAJERO')")
    public ResponseEntity<tarjetaResponseDTO> obtenerTarjetaActiva(Authentication authentication) {
        String correoPasajero = authentication.getName();
        tarjetaResponseDTO tarjeta = tarjetaService.obtenerTarjetaActiva(correoPasajero);
        return ResponseEntity.ok(tarjeta);
    }
}
