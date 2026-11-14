package com.example.MOVI.Config;

import com.example.MOVI.DTO.ResponseDTO.authResponse;
import com.example.MOVI.Service.serviceIMP.authService;
import jakarta.servlet.ServletException;
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


@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {


    private final authService authService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken =
                (OAuth2AuthenticationToken) authentication;

        String proveedor =
                oauthToken.getAuthorizedClientRegistrationId();

        OAuth2User oauthUser =
                (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");

        if (email == null) {
            email = oauthUser.getAttribute("preferred_username");
        }

        if (email == null) {
            email = oauthUser.getAttribute("userPrincipalName");
        }

        String nombre = oauthUser.getAttribute("name");

        String subject;

        if ("google".equals(proveedor)) {

            subject = oauthUser.getAttribute("sub");

            Boolean verified =
                    oauthUser.getAttribute("email_verified");

            if (!Boolean.TRUE.equals(verified)) {
                throw new BadCredentialsException(
                        "El correo de Google no está verificado"
                );
            }

        } else if ("microsoft".equals(proveedor)) {

            String oid = oauthUser.getAttribute("oid");
            String tid = oauthUser.getAttribute("tid");

            subject = tid != null && oid != null
                    ? tid + ":" + oid
                    : oauthUser.getAttribute("sub");

        } else {
            throw new BadCredentialsException(
                    "Proveedor no permitido"
            );
        }

        authResponse authResponse = authService.loginOAuth2(
                proveedor,
                subject,
                email,
                nombre
        );

        String targetUrl =
                "moviplus://login-success?token=" +
                        URLEncoder.encode(
                                authResponse.token(),
                                StandardCharsets.UTF_8
                        );


        response.sendRedirect(targetUrl);
    }
}