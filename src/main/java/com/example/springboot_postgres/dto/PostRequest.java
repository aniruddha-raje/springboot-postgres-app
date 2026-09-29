package com.example.springboot_postgres.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Body for creating or partially updating a local post. The id is generated
 * on create and taken from the path on update; the owning user comes from
 * the path on create.
 */
@Schema(description = "Post fields. The id is generated on create and taken from the path on update.")
public record PostRequest(
        @Schema(description = "Required on create; must not be blank on update", example = "Hello, world!")
        String content) implements StrictRequestBody {
}
