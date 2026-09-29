package com.example.springboot_postgres.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Body for creating or partially updating a role. */
@Schema(description = "Role fields. The id is generated on create and taken from the path on update.")
public record RoleRequest(
        @Schema(description = "Required on create; must not be blank on update", example = "EDITOR")
        String name) implements StrictRequestBody {
}
