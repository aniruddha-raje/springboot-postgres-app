package com.example.springboot_postgres.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * Name of the bearer-token scheme. Controllers behind the JWT filter
     * reference it with {@code @SecurityRequirement}, which puts a lock on
     * their operations in Swagger UI.
     */
    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Springboot Postgres API")
                        .description("Local users, profiles, posts and roles (JPA/Postgres), and a JSONPlaceholder /posts integration.")
                        .version("0.0.1"))
                // Adds the Authorize button: Swagger UI then sends
                // "Authorization: Bearer <token>" on secured operations.
                .components(new Components().addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
