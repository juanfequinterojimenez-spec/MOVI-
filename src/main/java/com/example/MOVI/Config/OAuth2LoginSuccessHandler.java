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
public class OAuth2LoginSuccessHandler
        implements AuthenticationSuccessHandler {

    private final authService authService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        if (!(authentication instanceof OAuth2AuthenticationToken token)) {
            throw new BadCredentialsException(
                    "Autenticación OAuth2 no válida"
            );
        }

        String proveedor = token
                .getAuthorizedClientRegistrationId()
                .toLowerCase(Locale.ROOT);

        OAuth2User user = token.getPrincipal();

        String subject;

        String email = primeroNoVacio(
                user.getAttribute("email"),
                user.getAttribute("preferred_username"),
                user.getAttribute("userPrincipalName")
        );

        String nombre = primeroNoVacio(
                user.getAttribute("name"),
                email
        );

        // Por defecto no asumimos que el correo esté verificado.
        boolean correoVerificado = false;

        // ==========================================
        // GOOGLE
        // ==========================================

        if ("google".equals(proveedor)) {

            subject = user.getAttribute("sub");

            Object atributoVerificado =
                    user.getAttribute("email_verified");

            boolean googleConfirmaCorreo =
                    Boolean.TRUE.equals(atributoVerificado);

            if (!googleConfirmaCorreo) {
                throw new BadCredentialsException(
                        "Correo Google no verificado"
                );
            }

            // Aceptamos directamente Gmail.
            // Para dominios externos se requiere comprobar
            // las garantías adicionales del proveedor.
            String dominioAlojado = user.getAttribute("hd");

            correoVerificado = email != null &&
                    (
                            email.toLowerCase(Locale.ROOT)
                                    .endsWith("@gmail.com")
                                    ||
                                    (
                                            dominioAlojado != null &&
                                                    !dominioAlojado.isBlank() &&
                                                    email.toLowerCase(Locale.ROOT)
                                                            .endsWith(
                                                                    "@" + dominioAlojado
                                                                            .toLowerCase(Locale.ROOT)
                                                            )
                                    )
                    );

            // ==========================================
            // MICROSOFT
            // ==========================================

        } else if ("microsoft".equals(proveedor)) {

            String tid = user.getAttribute("tid");
            String oid = user.getAttribute("oid");
            String sub = user.getAttribute("sub");

            if (tid == null || tid.isBlank()) {
                throw new BadCredentialsException(
                        "Falta tenant Microsoft"
                );
            }

            String identificador =
                    oid != null && !oid.isBlank()
                            ? oid
                            : sub;

            if (identificador == null ||
                    identificador.isBlank()) {

                throw new BadCredentialsException(
                        "Falta identificador Microsoft"
                );
            }

            subject = tid + ":" + identificador;

            // Microsoft no garantiza la propiedad del
            // buzón por el simple hecho de entregar
            // preferred_username o email.
            correoVerificado = false;

        } else {
            throw new BadCredentialsException(
                    "Proveedor OAuth2 no permitido"
            );
        }

        // ==========================================
        // VALIDAR IDENTIDAD
        // ==========================================

        if (subject == null ||
                subject.isBlank() ||
                email == null ||
                email.isBlank()) {

            throw new BadCredentialsException(
                    "El proveedor no suministró una identidad y correo válidos"
            );
        }

        // ==========================================
        // AUTENTICAR EN MOVI+
        // ==========================================

        authResponse resultado = authService.loginOAuth2(
                proveedor,
                subject,
                email,
                nombre,
                correoVerificado
        );

        // Redirección hacia Android MOVI+
        response.setHeader("Cache-Control", "no-store");

        response.sendRedirect(
                "movi://auth-success?token=" +
                        URLEncoder.encode(
                                resultado.token(),
                                StandardCharsets.UTF_8
                        )
        );
    }

    private static String primeroNoVacio(String... valores) {

        for (String valor : valores) {

            if (valor != null && !valor.isBlank()) {
                return valor.trim();
            }
        }

        return null;
    }
}
