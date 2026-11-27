package com.example.MOVI.Config;

import com.example.MOVI.Entity.Usuario;
import com.example.MOVI.Repository.usuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final usuarioRepository pasajeroRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.leer(cabecera.substring(7));
                // Los JWT destinados a QR no son credenciales de acceso a la API.
                if (claims.get("tipo") != null) {
                    chain.doFilter(request, response);
                    return;
                }
                Long id = ((Number) claims.get("id")).longValue();

                Optional<Usuario> usuarioOpt = pasajeroRepository.findById(id)
                        .filter(u -> u.getEmail() != null);

                if (usuarioOpt.isPresent()) {
                    Usuario u = usuarioOpt.get();

                    // Usuario bloqueado por un administrador: el token es válido pero
                    // la cuenta no puede operar. Se corta la cadena con 403.
                    if (!Boolean.TRUE.equals(u.getActivo())) {
                        SecurityContextHolder.clearContext();
                        SecurityErrorWriter.escribir(
                                response,
                                HttpStatus.FORBIDDEN,
                                "Tu cuenta ha sido bloqueada por un administrador",
                                request.getRequestURI()
                        );
                        return;
                    }

                    // 1. Creamos el objeto principal
                    UsuarioAutenticado principal =
                            new UsuarioAutenticado(u.getId(), u.getEmail(), u.getRol());

                    // 2. Pasamos ÚNICAMENTE el correo (String) como principal para que authentication.getName() funcione bien
                    UsernamePasswordAuthenticationToken autenticacion =
                            new UsernamePasswordAuthenticationToken(
                                    u.getEmail(), // <--- SOLO EL CORREO AQUÍ
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name())));

                    // 3. Opcional: Guardamos el objeto completo en los detalles por si lo requieres luego
                    autenticacion.setDetails(principal);

                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            } catch (JwtException | IllegalArgumentException | ClassCastException | NullPointerException e) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
