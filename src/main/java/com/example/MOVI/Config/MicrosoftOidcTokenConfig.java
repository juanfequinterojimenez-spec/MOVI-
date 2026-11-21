package com.example.MOVI.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenValidator;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtValidators;
import java.util.UUID;

@Configuration
public class MicrosoftOidcTokenConfig {
    @Bean
    public JwtDecoderFactory<ClientRegistration> idTokenDecoderFactory() {
        OidcIdTokenDecoderFactory factory = new OidcIdTokenDecoderFactory();
        factory.setJwtValidatorFactory(reg -> {
            OidcIdTokenValidator oidcValidator = new OidcIdTokenValidator(reg);
            OAuth2TokenValidator<Jwt> microsoftIssuerValidator = jwt -> {
                if (!"microsoft".equals(reg.getRegistrationId())) {
                    return OAuth2TokenValidatorResult.success();
                }
                String tid = jwt.getClaimAsString("tid");
                String issuer = jwt.getIssuer() == null ? "" : jwt.getIssuer().toString();
                try {
                    UUID.fromString(tid);
                } catch (Exception e) {
                    return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Microsoft tid inválido", null));
                }
                String expected = "https://login.microsoftonline.com/" + tid + "/v2.0";
                if (!expected.equals(issuer)) {
                    return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Issuer Microsoft no coincide con tid", null));
                }
                return OAuth2TokenValidatorResult.success();
            };
            return JwtValidators.createDefaultWithValidators(oidcValidator, microsoftIssuerValidator);
        });
        return factory;
    }
}
