package com.example.MOVI.Config;

import com.example.MOVI.DTO.ResponseDTO.authResponse;
import com.example.MOVI.Service.serviceIMP.authService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final authService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        if (!(authentication instanceof OAuth2AuthenticationToken token)) {
            throw new BadCredentialsException("Autenticación OAuth2 no válida");
        }
        String proveedor = token.getAuthorizedClientRegistrationId().toLowerCase(Locale.ROOT);
        OAuth2User user = token.getPrincipal();
        String subject;
        String email = primeroNoVacio(
            user.getAttribute("email"),
            user.getAttribute("preferred_username"),
            user.getAttribute("userPrincipalName")
        );
        String nombre = primeroNoVacio(user.getAttribute("name"), email);

        if ("google".equals(proveedor)) {
            subject = user.getAttribute("sub");
            if (!Boolean.TRUE.equals(user.getAttribute("email_verified"))) {
                throw new BadCredentialsException("Correo Google no verificado");
            }
        } else if ("microsoft".equals(proveedor)) {
            String tid = user.getAttribute("tid");
            String oid = user.getAttribute("oid");
            String sub = user.getAttribute("sub");
            if (tid == null || tid.isBlank()) {
                throw new BadCredentialsException("Falta tenant Microsoft");
            }
            // OID + TID identifican usuarios corporativos/educativos; SUB es alternativa personal.
            subject = tid + ":" + (oid != null && !oid.isBlank() ? oid : sub);
        } else {
            throw new BadCredentialsException("Proveedor OAuth2 no permitido");
        }
        if (subject == null || subject.isBlank() || email == null || email.isBlank()) {
            throw new BadCredentialsException("El proveedor no suministró una identidad y correo válidos");
        }
        // IMPORTANTE: email/preferred_username no acreditan beneficio educativo o laboral.
        // authService.loginOAuth2 debe preservar su protección de cuentas existentes.
        authResponse resultado = authService.loginOAuth2(proveedor, subject, email, nombre);

        // Compatibilidad con el actual deep-link Android. Cambiar por código de un solo uso
        // antes de producción para no exponer JWT en un URI que pueden interceptar otras apps.
        response.setHeader("Cache-Control", "no-store");
        response.sendRedirect("movi://auth-success?token=" +
            URLEncoder.encode(resultado.token(), StandardCharsets.UTF_8));
    }

    private static String primeroNoVacio(String... valores) {
        for (String valor : valores) {
            if (valor != null && !valor.isBlank()) return valor.trim();
        }
        return null;
    }
}
