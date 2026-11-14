package com.example.MOVI.Entity;

import com.example.MOVI.Enum.EstadoCanje;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Canjes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Canje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long idCanje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuarioId", nullable = false)
    @JsonIgnore // CRÍTICO: Evita bucles infinitos (StackOverflow) en el JSON
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recompensaId", nullable = false)
    @JsonIgnore // CRÍTICO: Evita que al ver el canje se traiga todo el aliado y sus ofertas
    private Recompensa recompensa;

    @Column(name = "puntosUsados", nullable = false)
    private Integer puntosGastados;

    @Column(name = "fechaCanjes", nullable = false)
    private LocalDateTime fechaCanje;

    // Sugerencia añadida: Útil para que la persona vaya al cine/restaurante y muestre un código
    @Column(name = "codigo_comprobante", unique = true)
    private String codigoComprobante;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoCanje estado = EstadoCanje.PENDIENTE;

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    @PrePersist
    protected void onCreate() {
        if (this.fechaCanje == null) {
            this.fechaCanje = LocalDateTime.now();
        }
        if (this.fechaVencimiento == null) {
            this.fechaVencimiento = this.fechaCanje.plusDays(30); // Vigencia por defecto: 30 días
        }
    }

}
