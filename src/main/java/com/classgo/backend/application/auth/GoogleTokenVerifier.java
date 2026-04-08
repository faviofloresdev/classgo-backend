package com.classgo.backend.application.auth;

import com.classgo.backend.infrastructure.config.AppProperties;
import com.classgo.backend.shared.exception.BusinessRuleViolationException;
import java.util.List;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class GoogleTokenVerifier {

    private static final String GOOGLE_JWK_SET_URI = "https://www.googleapis.com/oauth2/v3/certs";
    private static final List<String> GOOGLE_ISSUERS = List.of("https://accounts.google.com", "accounts.google.com");

    private final AppProperties appProperties;
    private final JwtDecoder jwtDecoder;

    public GoogleTokenVerifier(AppProperties appProperties) {
        this.appProperties = appProperties;
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(GOOGLE_JWK_SET_URI).build();
        decoder.setJwtValidator(tokenValidator());
        this.jwtDecoder = decoder;
    }

    public GoogleUser verify(String idToken) {
        if (!appProperties.google().enabled()) {
            throw new BusinessRuleViolationException("Google auth is disabled in this environment");
        }
        if (!StringUtils.hasText(appProperties.google().clientId())) {
            throw new BusinessRuleViolationException("Google auth is not configured correctly");
        }

        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(idToken);
        } catch (Exception ex) {
            throw new BusinessRuleViolationException("Invalid Google token");
        }

        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");
        Boolean emailVerified = jwt.getClaim("email_verified");

        if (!StringUtils.hasText(email)) {
            throw new BusinessRuleViolationException("Google token does not contain an email");
        }
        if (!Boolean.TRUE.equals(emailVerified)) {
            throw new BusinessRuleViolationException("Google account email is not verified");
        }

        return new GoogleUser(email.toLowerCase(), StringUtils.hasText(name) ? name : email, true);
    }

    private OAuth2TokenValidator<Jwt> tokenValidator() {
        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefault();
        OAuth2TokenValidator<Jwt> issuerValidator = jwt -> GOOGLE_ISSUERS.contains(jwt.getIssuer() != null ? jwt.getIssuer().toString() : null)
            ? OAuth2TokenValidatorResult.success()
            : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid Google token issuer", null));
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> jwt.getAudience().contains(appProperties.google().clientId())
            ? OAuth2TokenValidatorResult.success()
            : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Google token audience does not match client id", null));
        return jwt -> {
            OAuth2TokenValidatorResult result = defaultValidator.validate(jwt);
            if (result.hasErrors()) {
                return result;
            }
            result = issuerValidator.validate(jwt);
            if (result.hasErrors()) {
                return result;
            }
            return audienceValidator.validate(jwt);
        };
    }

    public record GoogleUser(String email, String name, boolean emailVerified) {}
}
