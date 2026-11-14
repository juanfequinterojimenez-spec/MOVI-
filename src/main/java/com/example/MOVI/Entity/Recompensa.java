package com.example.MOVI.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Recompensas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recompensa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long idRecompensa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aliadoId", nullable = false)
    private Aliado aliado;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcionRecompensa;

    @Column(name = "stock", nullable = false)
    private Integer stockDisponible;

    @Column(name = "costoPuntos", nullable = false)
    private Integer costoPuntos;


    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(name = "activa")
    @Builder.Default
    private Boolean activa = true;
}
