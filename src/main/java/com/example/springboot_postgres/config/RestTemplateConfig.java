package com.example.springboot_postgres.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    /**
     * Backed by the JDK {@link java.net.http.HttpClient} (via
     * {@link JdkClientHttpRequestFactory}) rather than the default
     * {@code SimpleClientHttpRequestFactory}. The default factory uses
     * {@code HttpURLConnection}, which cannot issue HTTP PATCH requests and
     * fails with "Invalid HTTP method: PATCH". The JDK client supports every
     * method, so the JSONPlaceholder PATCH integration works reliably.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(new JdkClientHttpRequestFactory());
    }
}
