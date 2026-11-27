package com.example.MOVI.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "sesiones_persistentes", indexes = {
        @Index(name = "idx_sesiones_usuario", columnList = "usuario_id"),
        @Index(name = "idx_sesiones_hash", columnList = "token_hash", unique = true)
})
@Getter @Setter
public class SesionPersistente {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(name = "creada_en", nullable = false)
    private Instant creadaEn;

    @Column(name = "vence_en", nullable = false)
    private Instant venceEn;

    @Column(name = "revocada_en")
    private Instant revocadaEn;
}
