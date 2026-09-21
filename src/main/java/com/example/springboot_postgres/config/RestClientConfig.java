package com.example.springboot_postgres.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /**
     * {@link RestClient} for JSONPlaceholder, rooted at the {@code /posts} base
     * URL so calls only supply the path below it (e.g. {@code "/{id}"}). Uses
     * the same JDK {@link java.net.http.HttpClient} factory as the
     * {@code RestTemplate} so PATCH works (see {@link RestTemplateConfig}).
     */
    @Bean
    public RestClient jsonPlaceholderRestClient(AppProperties properties) {
        return RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory())
                .baseUrl(properties.getJsonPlaceholderUrl())
                .build();
    }
}
