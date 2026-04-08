package com.classgo.backend.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI classGoOpenApi() {
        return new OpenAPI()
            .info(new Info().title("ClassGo Backend API").version("v1").description("API para aulas, planes, topicos y gameplay con JWT"))
            .components(new Components().addSecuritySchemes(
                "bearer-jwt",
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
            ))
            .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
