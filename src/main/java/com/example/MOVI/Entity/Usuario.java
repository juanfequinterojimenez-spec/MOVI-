package com.example.MOVI.Entity;

import com.example.MOVI.Enum.EstadoAliado;
import com.example.MOVI.Enum.Rol;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table(name = "pasajeros")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "nombreCompleto") // Quitamos nullable = false
    private String nombreCompleto;

    @Column(name = "tipo_documento") // Quitamos nullable = false
    private String tipoDocumento;

    @Column(name = "documentoIdentidad", unique = true, length = 50) // Quitamos nullable = false
    private String documento;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Builder.Default
    @Column(name = "puntos", nullable = false)
    private Integer puntos = 0;

    private String fotoPerfilURL;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private Rol rol = Rol.PASAJERO;

    // Bandera de bloqueo administrativo. Si es false, el JwtAuthenticationFilter
    // rechaza la petición con 403 aunque el token sea válido.
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Builder.Default
    @Column(name = "correo_verificado", nullable = false)
    private Boolean correoVerificado = true;

}



