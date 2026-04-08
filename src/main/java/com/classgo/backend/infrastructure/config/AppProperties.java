package com.classgo.backend.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
    Jwt jwt,
    Cors cors,
    Google google,
    Storage storage,
    Notifications notifications,
    Aws aws
) {

    public record Jwt(@NotBlank String secret, @NotNull Duration accessTokenExpiration, @NotNull Duration refreshTokenExpiration) {}

    public record Cors(List<String> allowedOrigins) {}

    public record Google(boolean enabled, String clientId) {}

    public record Storage(@NotBlank String publicBaseUrl, @NotBlank String bucket) {}

    public record Notifications(boolean emailEnabled) {}

    public record Aws(@NotBlank String region, String accessKeyId, String secretAccessKey, Ses ses, R2 r2) {}

    public record Ses(@NotBlank String fromEmail) {}

    public record R2(String endpoint) {}
}
