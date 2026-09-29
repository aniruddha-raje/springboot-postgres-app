package com.example.springboot_postgres.controller;

import com.example.springboot_postgres.config.OpenApiConfig;
import com.example.springboot_postgres.dto.ProfileRequest;
import com.example.springboot_postgres.model.Profile;
import com.example.springboot_postgres.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** A user's profile. Each user has at most one, so it is addressed through the user. */
@Slf4j
@Tag(name = "Profiles", description = "A user's profile (JPA/Postgres)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/user/id/{userId}/profile")
public class ProfileController {

    @Autowired
    ProfileService service;

    @Operation(summary = "Get a user's profile")
    @GetMapping
    public ResponseEntity<Profile> getProfile(@PathVariable Long userId) {
        log.info("getProfile called with userId={}", userId);
        return new ResponseEntity<>(service.getProfile(userId), HttpStatus.OK);
    }

    @Operation(summary = "Create or replace a user's profile")
    @PutMapping
    public ResponseEntity<Profile> putProfile(@PathVariable Long userId, @RequestBody ProfileRequest request) {
        log.info("putProfile called with userId={}", userId);
        return new ResponseEntity<>(service.putProfile(userId, request), HttpStatus.OK);
    }

    @Operation(summary = "Delete a user's profile")
    @DeleteMapping
    public ResponseEntity<Void> deleteProfile(@PathVariable Long userId) {
        log.info("deleteProfile called with userId={}", userId);
        service.deleteProfile(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
