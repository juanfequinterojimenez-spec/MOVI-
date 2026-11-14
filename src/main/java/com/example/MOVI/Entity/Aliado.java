package com.example.MOVI.Entity;

import com.example.MOVI.Enum.categoriaAliado;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "aliados")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aliado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuarioId", nullable = false, unique = true)
    @JsonIgnore
    private Usuario usuario;

    @Column(name = "nombre_aliado", nullable = false)
    private String nombreAliado;


    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private categoriaAliado CategoriaAliado;


    @Column(name = "email_contacto")
    private String correoContacto;


    @Column(name = "logo_url")
    private String logoUrl;


    @OneToMany(mappedBy = "aliado", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Recompensa> recompensas = new ArrayList<>();
}
