package com.example.springboot_postgres.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public liveness/metadata endpoints. These are intentionally left open
 * (not covered by the auth filter).
 */
@Tag(name = "Health", description = "Public health and version endpoints")
@RestController
public class HealthController {

    private static final String API_VERSION = "2.0";

    @Operation(summary = "Health check")
    @GetMapping("/health-check")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return new ResponseEntity<>(Map.of("status", "ok"), HttpStatus.OK);
    }

    @Operation(summary = "API version")
    @GetMapping("/version")
    public ResponseEntity<Map<String, String>> version() {
        return new ResponseEntity<>(Map.of("version", API_VERSION), HttpStatus.OK);
    }
}
