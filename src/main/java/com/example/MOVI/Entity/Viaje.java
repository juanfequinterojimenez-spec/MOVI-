package com.example.MOVI.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "viajes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Viaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long idViaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuarioId", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarjetaId", nullable = false)
    private Tarjeta tarjeta;

    @Column(name = "nombreEstacion", nullable = false)
    private String nombreEstacion;

    @Column(name = "pointsGenerados", nullable = false)
    private Integer puntosGenerados;

    @Column(name = "tiempoViaje", nullable = false)
    private LocalDateTime fechaHoraViaje;

    @Column(name = "es_transbordo_gratuito", nullable = false)
    private boolean esTransbordoGratuito;

    @Column(name = "tipo_componente")
    private String tipoComponente;

    @PrePersist
    public void prePersist() {
        this.fechaHoraViaje = LocalDateTime.now();
    }
}

