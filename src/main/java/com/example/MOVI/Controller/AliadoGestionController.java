package com.example.MOVI.Controller;

import com.example.MOVI.Entity.Aliado;
import com.example.MOVI.Entity.Canje;
import com.example.MOVI.Entity.Recompensa;
import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Enum.EstadoCanje;
import com.example.MOVI.Exception.BusinessRuleException;
import com.example.MOVI.Exception.ResourceNotFoundException;
import com.example.MOVI.Repository.aliadoRepository;
import com.example.MOVI.Repository.canjeRepository;
import com.example.MOVI.Repository.recompensaRepository;
import com.example.MOVI.Repository.usuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/aliados/gestion")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ALIADO')")
public class AliadoGestionController {
    private final usuarioRepository usuarios;
    private final aliadoRepository aliados;
    private final recompensaRepository recompensas;
    private final canjeRepository canjes;

    public record ProductoDto(Long id, String titulo, String descripcion, int puntos,
                              int stock, String imagenUrl, String estado) {
        public static ProductoDto desde(Recompensa r) {
            return new ProductoDto(r.getIdRecompensa(), r.getTitulo(), r.getDescripcionRecompensa(),
                    r.getCostoPuntos(), r.getStockDisponible(), r.getImagenUrl(),
                    Boolean.TRUE.equals(r.getActiva()) ? "VALIDADO" : "PENDIENTE");
        }
    }
    public record PerfilDto(Long id, String nombreMarca, String categoria, String correo, String logoUrl) {}
    public record EstadisticasDto(long productos, long pendientes, long canjesHoy) {}
    public record EdicionDto(@NotBlank String titulo, String descripcion,
                             @NotNull @Min(1) Integer puntos,
                             @NotNull @Min(0) Integer stock) {}
    public record CuponDto(String codigo, String tituloProducto, int puntos, String cliente, String estado) {}

    private Aliado actual(Authentication auth) {
        Usuario usuario = usuarios.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return aliados.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de aliado no encontrado"));
    }

    @GetMapping("/perfil")
    @Transactional(readOnly = true)
    public PerfilDto perfil(Authentication auth) {
        Aliado a = actual(auth);
        return new PerfilDto(a.getId(), a.getNombreAliado(), a.getCategoriaAliado().name(),
                a.getCorreoContacto(), a.getLogoUrl());
    }

    @GetMapping("/productos")
    @Transactional(readOnly = true)
    public List<ProductoDto> productos(Authentication auth) {
        return recompensas.findByAliado_Id(actual(auth).getId()).stream()
                .map(ProductoDto::desde).toList();
    }

    @GetMapping("/estadisticas")
    @Transactional(readOnly = true)
    public EstadisticasDto estadisticas(Authentication auth) {
        Aliado a = actual(auth);
        List<Recompensa> lista = recompensas.findByAliado_Id(a.getId());
        long pendientes = lista.stream().filter(r -> !Boolean.TRUE.equals(r.getActiva())).count();
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        long hoy = canjes.countByRecompensa_Aliado_IdAndEstadoAndFechaEntregaGreaterThanEqualAndFechaEntregaLessThan(
                a.getId(), EstadoCanje.UTILIZADO, inicio, inicio.plusDays(1));
        return new EstadisticasDto(lista.size(), pendientes, hoy);
    }

    @PutMapping("/productos/{id}")
    @Transactional
    public ProductoDto editar(@PathVariable Long id, @Valid @RequestBody EdicionDto dto, Authentication auth) {
        Aliado a = actual(auth);
        Recompensa r = recompensas.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        if (!r.getAliado().getId().equals(a.getId()))
            throw new BusinessRuleException("Este producto no pertenece a tu negocio");
        r.setTitulo(dto.titulo().trim());
        r.setDescripcionRecompensa(dto.descripcion() == null ? "" : dto.descripcion().trim());
        r.setCostoPuntos(dto.puntos());
        r.setStockDisponible(dto.stock());
        // Requiere nueva validación si se cambia la oferta; no debe publicarse automáticamente.
        r.setActiva(false);
        return ProductoDto.desde(recompensas.save(r));
    }

    @GetMapping("/canjes/{codigo}")
    @Transactional(readOnly = true)
    public CuponDto consultar(@PathVariable String codigo, Authentication auth) {
        Aliado a = actual(auth);
        Canje c = canjes.findByCodigoComprobante(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Código no encontrado"));
        if (!c.getRecompensa().getAliado().getId().equals(a.getId()))
            throw new BusinessRuleException("El cupón pertenece a otro aliado");
        if (c.getEstado() != EstadoCanje.PENDIENTE && c.getEstado() != EstadoCanje.VIGENTE)
            throw new BusinessRuleException("Este cupón no está pendiente de entrega");
        if (c.getFechaVencimiento() != null && LocalDateTime.now().isAfter(c.getFechaVencimiento()))
            throw new BusinessRuleException("Este cupón ha vencido");
        String nombre = c.getUsuario().getNombreCompleto();
        return new CuponDto(c.getCodigoComprobante(), c.getRecompensa().getTitulo(),
                c.getPuntosGastados(), nombre == null ? "Cliente MOVI+" : nombre, c.getEstado().name());
    }
}
