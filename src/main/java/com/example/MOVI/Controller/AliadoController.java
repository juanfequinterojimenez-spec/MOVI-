package com.example.MOVI.Controller;


import com.example.MOVI.DTO.ResponseDTO.AliadoRegistroDTO;
import com.example.MOVI.DTO.ResponseDTO.AliadoResponseDTO;
import com.example.MOVI.DTO.ResponseDTO.PerfilAliadoDto;
import com.example.MOVI.DTO.ResponseDTO.recompensaRequestDTO;
import com.example.MOVI.Entity.Recompensa;
import com.example.MOVI.Enum.categoriaAliado;
import com.example.MOVI.Service.serviceIMP.aliadoService;
import com.example.MOVI.Service.serviceIMP.recompensaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/aliados")
@RequiredArgsConstructor
    public class AliadoController {

    private final aliadoService aliadoService;
    private final recompensaService recompensaService;


    @GetMapping("/catalogo")
    @PreAuthorize("hasRole('PASAJERO')")
    public ResponseEntity<List<Recompensa>> verCatalogo(
            @RequestParam(name = "categoria", required = false) String categoria) {

        List<Recompensa> catalogo = recompensaService.obtenerCatalogo(categoria);
        return ResponseEntity.ok(catalogo);
    }


    @GetMapping("/mis-publicaciones")
    @PreAuthorize("hasRole('ALIADO')")
    public ResponseEntity<List<Recompensa>> verMisOfertas(Authentication authentication) {
        String correoAliado = authentication.getName();
        List<Recompensa> misOfertas = aliadoService.obtenerOfertasPorAliado(correoAliado);
        return ResponseEntity.ok(misOfertas);
    }


    @PatchMapping(value = "/perfil", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ALIADO')")
    public ResponseEntity<String> actualizarPerfilMarca(
            @RequestPart("dto") @Valid PerfilAliadoDto dto,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            Authentication authentication) {

        // Extraemos el correo del token JWT para saber qué aliado está haciendo la petición
        String correoAliado = authentication.getName();

        aliadoService.actualizarPerfil(correoAliado, dto, logo);
        return ResponseEntity.ok("Perfil de marca actualizado exitosamente");
    }

    @PostMapping(value = "/recompensas/crear", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ALIADO')")
    public ResponseEntity<Recompensa> publicarOferta(
            @RequestParam("titulo") String titulo,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("puntosRequeridos") Integer puntosRequeridos,
            @RequestParam(value = "stock", required = false) Integer stock,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,
            Authentication authentication) {

        String correoAliado = authentication.getName();
        recompensaRequestDTO request = new recompensaRequestDTO(titulo, descripcion, puntosRequeridos, stock);

        Recompensa nuevaOferta = aliadoService.publicarOferta(correoAliado, request, imagen);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaOferta);
    }

    @PostMapping(value = "/registro", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AliadoResponseDTO> registrarAliado(
            @RequestParam("correo") String correo,
            @RequestParam("password") String password,
            @RequestParam("nombreMarca") String nombreMarca,
            @RequestParam("categoria") String categoria,
            @RequestPart(value = "logo", required = false) MultipartFile logo) {

        AliadoRegistroDTO dto = new AliadoRegistroDTO(correo, password, nombreMarca, categoria);
        AliadoResponseDTO response = aliadoService.registrarAliado(dto, logo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}



