package com.example.MOVI.Controller;

import com.example.MOVI.Entity.*;
import com.example.MOVI.Repository.*;
import com.example.MOVI.Enum.Rol;
import com.example.MOVI.Service.serviceIMP.usuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/movil")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminMovilController {
    private final usuarioRepository usuarios;
    private final tarjetaRepository tarjetas;
    private final viajeRepository viajes;
    private final canjeRepository canjes;
    private final recompensaRepository recompensas;
    private final usuarioService.UsuarioService usuarioService;

    public record Stats(long totalRecompensas, long recompensasActivas, long canjesRealizados,
                        long usuariosRegistrados, long tarjetasVinculadas) {}
    public record TarjetaInfo(Long id, String serial, String estado, Integer saldo) {}
    public record UsuarioInfo(Long id, String nombre, String email, String documento,
                              String tipoDocumento, List<TarjetaInfo> tarjetas) {}
    public record CanjeInfo(Long id, String codigo, Long usuarioId, String nombreUsuario,
                            String correo, String documento, String recompensa,
                            Integer puntos, String aliado, String fecha, String estado) {}
    public record ViajeInfo(Long id, Long usuarioId, String estacion, String serialTarjeta,
                            String fecha, Integer puntos, String tipoComponente, Integer tarifa) {}
    public record RecompensaInfo(Long id, String titulo, String descripcion, Integer puntos,
                                 Integer stock, Boolean activa, String aliado) {}

    @GetMapping("/resumen")
    @Transactional(readOnly = true)
    public Stats resumen() {
        return new Stats(recompensas.count(), recompensas.findByActivaTrue().size(),
                canjes.count(), usuarios.count(), tarjetas.count());
    }

    @GetMapping("/usuarios")
    @Transactional(readOnly = true)
    public List<UsuarioInfo> listarUsuarios(@RequestParam(defaultValue = "") String buscar) {
        String q = buscar.trim().toLowerCase(Locale.ROOT);
        return usuarios.findAll().stream().filter(u -> q.isBlank()
                || Objects.toString(u.getId(), "").contains(q)
                || Objects.toString(u.getEmail(), "").toLowerCase(Locale.ROOT).contains(q)
                || Objects.toString(u.getDocumento(), "").toLowerCase(Locale.ROOT).contains(q))
            .map(u -> new UsuarioInfo(u.getId(), u.getNombreCompleto(), u.getEmail(),
                    u.getDocumento(), u.getTipoDocumento(),
                    tarjetas.findByUsuarioId(u.getId()).map(t -> List.of(mapTarjeta(t))).orElseGet(List::of)))
            .toList();
    }
    private TarjetaInfo mapTarjeta(Tarjeta t) {
        // El esquema actual no tiene saldo de tuLlave; no fabricamos uno.
        return new TarjetaInfo(t.getIdTarjeta(), t.getNumeroSerie(),
                t.getEstado() == null ? "DESCONOCIDO" : t.getEstado().name(), null);
    }

    @DeleteMapping("/usuarios/{id}")
    @Transactional
    public ResponseEntity<Map<String,String>> eliminarUsuario(@PathVariable Long id) {
        Usuario u = usuarios.findById(id).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        if (u.getRol() == Rol.ADMINISTRADOR) {
            return ResponseEntity.status(403).body(Map.of("mensaje", "No se puede eliminar una cuenta administradora"));
        }
        // Las relaciones de viajes, tarjetas y canjes pueden impedir borrar por FK: el servidor
        // devolverá error controlado; no se borran historiales silenciosamente.
        usuarioService.eliminar(id);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado"));
    }

    @GetMapping("/canjes")
    @Transactional(readOnly = true)
    public List<CanjeInfo> listarCanjes(@RequestParam(defaultValue = "") String buscar) {
        String q = buscar.trim().toLowerCase(Locale.ROOT);
        return canjes.findAll().stream().map(c -> {
            Usuario u = c.getUsuario(); Recompensa r = c.getRecompensa();
            return new CanjeInfo(c.getIdCanje(), c.getCodigoComprobante(), u.getId(),
                    u.getNombreCompleto(), u.getEmail(), u.getDocumento(), r.getTitulo(),
                    c.getPuntosGastados(), r.getAliado() == null ? "" : r.getAliado().getNombreAliado(),
                    Objects.toString(c.getFechaCanje(), ""),
                    c.getEstado() == null ? "" : c.getEstado().name());
        }).filter(c -> q.isBlank() || (c.codigo() + " " + c.nombreUsuario() + " " + c.correo()
                + " " + c.usuarioId() + " " + c.documento()).toLowerCase(Locale.ROOT).contains(q))
                .sorted(Comparator.comparing(CanjeInfo::fecha).reversed()).toList();
    }

    @GetMapping("/viajes")
    @Transactional(readOnly = true)
    public List<ViajeInfo> listarViajes(@RequestParam(required = false) Long usuarioId,
                                       @RequestParam(required = false) Long viajeId) {
        return viajes.findAll().stream()
            .filter(v -> usuarioId == null || v.getUsuario().getId().equals(usuarioId))
            .filter(v -> viajeId == null || v.getIdViaje().equals(viajeId))
            .map(v -> new ViajeInfo(v.getIdViaje(), v.getUsuario().getId(), v.getNombreEstacion(),
                    v.getTarjeta() == null ? "" : v.getTarjeta().getNumeroSerie(),
                    Objects.toString(v.getFechaHoraViaje(), ""), v.getPuntosGenerados(),
                    v.getTipoComponente(), null))
            .sorted(Comparator.comparing(ViajeInfo::fecha).reversed()).toList();
    }

    @GetMapping("/recompensas")
    @Transactional(readOnly = true)
    public List<RecompensaInfo> listarRecompensas() {
        return recompensas.findAll().stream()
            .map(r -> new RecompensaInfo(r.getIdRecompensa(), r.getTitulo(),
                    r.getDescripcionRecompensa(), r.getCostoPuntos(), r.getStockDisponible(),
                    r.getActiva(), r.getAliado() == null ? "" : r.getAliado().getNombreAliado()))
            .toList();
    }
}
