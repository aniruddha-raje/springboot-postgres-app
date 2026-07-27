package com.example.springboot_postgres.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externalised configuration bound from the {@code app.*} keys in
 * {@code application.yml}. Keeps the JSONPlaceholder base URL out of the code.
 */
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
@Component
public class AppProperties {

    private String jsonPlaceholderUrl;
}
