package com.example.springboot_postgres.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI().info(new Info()
                .title("Springboot Postgres API")
                .description("Local users, profiles, posts and roles (JPA/Postgres), and a JSONPlaceholder /posts integration.")
                .version("0.0.1"));
    }
}
