package com.example.MOVI.Entity;

import jakarta.persistence.*;
import lombok.Data;


import java.time.Instant;

@Entity
@Table(name = "verificaciones_correo")
@Data
public class VerificacionCorreo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "codigo_hash", nullable = false)
    private String codigoHash;

    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;

    @Column(name = "ultimo_envio", nullable = false)
    private Instant ultimoEnvio;

    @Column(nullable = false)
    private int intentos = 0;
}
