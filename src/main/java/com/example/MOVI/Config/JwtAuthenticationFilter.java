package com.example.MOVI.Config;

import com.example.MOVI.Repository.usuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

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
                Long id = ((Number) claims.get("id")).longValue();

                pasajeroRepository.findById(id)
                        .filter(u -> u.getEmail() != null)
                        .ifPresent(u -> {
                            // 1. Creamos tu objeto principal
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
                        });
            } catch (JwtException | IllegalArgumentException | ClassCastException | NullPointerException e) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}