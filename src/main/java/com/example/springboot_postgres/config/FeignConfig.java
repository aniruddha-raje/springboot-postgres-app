package com.example.springboot_postgres.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Enables scanning for {@code @FeignClient} interfaces. Kept off the main
 * application class so {@code @WebMvcTest} slices don't try to build Feign
 * clients.
 */
@Configuration
@EnableFeignClients(basePackages = "com.example.springboot_postgres.client")
public class FeignConfig {
}
