
package com.example.MOVI.Entity;

import com.example.MOVI.Enum.TipoInstitucion;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "dominios_institucionales",
        uniqueConstraints = @UniqueConstraint(
                columnNames = "dominio"
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DominioInstitucional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "nombre_institucion",
            nullable = false,
            length = 150
    )
    private String nombreInstitucion;

    @Column(
            name = "dominio",
            nullable = false,
            length = 253
    )
    private String dominio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoInstitucion tipo;

    @Builder.Default
    @Column(nullable = false)
    private Boolean verificado = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = false;
}

