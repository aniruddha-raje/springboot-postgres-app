package com.example.springboot_postgres.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Body for creating or partially updating a user. The id is never part of
 * the body: it is generated on create and taken from the path on update.
 * Profile, posts and roles are managed through their own endpoints.
 */
@Schema(description = "User fields. The id is generated on create and taken from the path on update.")
public record UserRequest(
        @Schema(description = "Required on create; must not be blank on update", example = "jane_doe")
        String username,
        @Schema(example = "jane@example.com")
        String email) implements StrictRequestBody {
}
