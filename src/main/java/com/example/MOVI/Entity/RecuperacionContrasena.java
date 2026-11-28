package com.example.MOVI.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recuperaciones_contrasena", indexes = {
    @Index(name = "idx_recuperacion_usuario", columnList = "usuario_id")
})
@Getter @Setter
public class RecuperacionContrasena {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "codigo_hash", nullable = false)
    private String codigoHash;

    @Column(name = "vence_en", nullable = false)
    private Instant venceEn;

    @Column(name = "ultimo_envio", nullable = false)
    private Instant ultimoEnvio;

    @Column(nullable = false)
    private int intentos;
}
