package com.example.MOVI.Entity;

import com.example.MOVI.Enum.EstadoVerificacionTarjeta;
import com.example.MOVI.Enum.estadoTarjeta;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tarjetas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tarjeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long idTarjeta;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "usuarioId",
            nullable = false,
            unique = true
    )
    private Usuario usuario;

    @Column(
            name = "serial",
            nullable = false,
            unique = true,
            length = 16
    )
    private String numeroSerie;

    // Estado operativo de la tarjeta
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private estadoTarjeta estado = estadoTarjeta.ACTIVA;

    // Estado de verificación de personalización
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado_verificacion",
            nullable = false,
            length = 35
    )
    private EstadoVerificacionTarjeta estadoVerificacion =
            EstadoVerificacionTarjeta.PENDIENTE;

    // Evidencia opcional almacenada de forma privada
    @Column(name = "foto_evidencia_url")
    private String fotoEvidenciaUrl;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_verificacion")
    private LocalDateTime fechaVerificacion;

    @Column(name = "observaciones_revision", length = 500)
    private String observacionesRevision;

    @Column(name = "vinculaciones", nullable = false)
    private LocalDateTime fechaVinculacion;

    @PrePersist
    public void prePersist() {

        LocalDateTime ahora = LocalDateTime.now();

        if (fechaVinculacion == null) {
            fechaVinculacion = ahora;
        }

        if (fechaSolicitud == null) {
            fechaSolicitud = ahora;
        }

        if (estadoVerificacion == null) {
            estadoVerificacion =
                    EstadoVerificacionTarjeta.PENDIENTE;
        }
    }
}