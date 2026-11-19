package com.example.MOVI.Entity;

import com.example.MOVI.Enum.ProveedorIdentidad;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
        name = "identidades_externas",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"proveedor", "subject"}
                )
        }
)
@Data
public class IdentidadExterna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProveedorIdentidad proveedor;

    @Column(nullable = false)
    private String subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
