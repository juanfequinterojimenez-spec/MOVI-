package com.example.MOVI.Entity;

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
    @JoinColumn(name = "usuarioId", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "serial", nullable = false, unique = true, length = 16)
    private String numeroSerie;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private estadoTarjeta estado = estadoTarjeta.ACTIVA;

    @Column(name = "vinculaciones", nullable = false)
    private LocalDateTime fechaVinculacion;

    @PrePersist
    public void prePersist() {
        this.fechaVinculacion = LocalDateTime.now();
    }
}
