
package com.example.MOVI.Config;

import com.nimbusds.jwt.SignedJWT;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenValidator;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;

import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.text.ParseException;
import java.util.Locale;

@Configuration
public class MicrosoftOidcConfig {

    // Solo identificadores de tenant con formato UUID.
    private static final String TENANT_PATTERN =
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-" +
                    "[0-9a-f]{4}-[0-9a-f]{12}$";

    @Bean
    public JwtDecoderFactory<ClientRegistration>
    idTokenDecoderFactory() {

        // Google conserva su decodificador habitual.
        OidcIdTokenDecoderFactory decoderNormal =
                new OidcIdTokenDecoderFactory();

        return registration -> {

            if (!"microsoft".equals(
                    registration.getRegistrationId())) {

                return decoderNormal.createDecoder(
                        registration
                );
            }

            return crearDecoderMicrosoft(registration);
        };
    }

    private JwtDecoder crearDecoderMicrosoft(
            ClientRegistration registration
    ) {

        return token -> {

            final String tenantId;

            try {
                // Se lee el tenant solo para seleccionar
                // el emisor Microsoft que debe validarlo.
                // Estos datos aún NO son confiables.
                SignedJWT jwtSinValidar =
                        SignedJWT.parse(token);

                Object tid =
                        jwtSinValidar.getJWTClaimsSet()
                                .getClaim("tid");

                if (!(tid instanceof String valor)) {
                    throw new BadJwtException(
                            "Falta tenant Microsoft"
                    );
                }

                tenantId = valor
                        .toLowerCase(Locale.ROOT);

            } catch (ParseException e) {
                throw new BadJwtException(
                        "ID token Microsoft inválido", e
                );
            }

            if (!tenantId.matches(TENANT_PATTERN)) {
                throw new BadJwtException(
                        "Identificador de tenant inválido"
                );
            }

            // Se construye un emisor Microsoft específico.
            // No se aceptan URLs de emisores arbitrarios.
            String issuer =
                    "https://login.microsoftonline.com/"
                            + tenantId + "/v2.0";

            // Descubre las claves desde el emisor real.
            // Se valida la firma criptográfica.
            NimbusJwtDecoder decoder =
                    NimbusJwtDecoder
                            .withIssuerLocation(issuer)
                            .build();

            // Validación estándar JWT + comprobaciones OIDC.
            decoder.setJwtValidator(
                    new DelegatingOAuth2TokenValidator<>(
                            JwtValidators.createDefaultWithIssuer(
                                    issuer
                            ),
                            new OidcIdTokenValidator(
                                    registration
                            )
                    )
            );

            decoder.setClaimSetConverter(
                    OidcIdTokenDecoderFactory
                            .createDefaultClaimTypeConverter()
            );

            var jwt = decoder.decode(token);

            // Comprobar nuevamente el tenant,
            // ahora sobre el token firmado y validado.
            String tenantValidado =
                    jwt.getClaimAsString("tid");

            if (!tenantId.equalsIgnoreCase(
                    tenantValidado)) {
                throw new BadJwtException(
                        "El tenant del token no coincide"
                );
            }

            return jwt;
        };
    }
}

