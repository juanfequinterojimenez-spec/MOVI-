package com.example.MOVI.Config;

import com.example.MOVI.Entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.expiration-ms}") long expiracionMs) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    // Genera el token usando el correo como subject y claims personalizados
    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("id", usuario.getId())
                .claim("rol", usuario.getRol().name())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    // Lee y valida los claims del token (JJWT 0.12.x)
    public Claims leer(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 🆕 Generación de token QR temporal adaptada a la sintaxis de JJWT 0.12.x
    public String generarTokenRedencion(Long usuarioId, long expiracionMilisegundos) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiracionMilisegundos);

        return Jwts.builder()
                .subject(usuarioId.toString())
                .claim("id", usuarioId)
                .claim("tipo", "REDENCION_QR")
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(clave) // Usamos la variable 'clave' directamente
                .compact();
    }

    public boolean validarTokenRedencion(String token) {
        try {
            Claims claims = leer(token);
            String tipo = claims.get("tipo", String.class);
            return "REDENCION_QR".equals(tipo);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}