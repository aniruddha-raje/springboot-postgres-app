package com.example.springboot_postgres.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Body for creating or replacing a user's profile; the user comes from the path. */
@Schema(description = "Profile fields. The profile is identified by the user id in the path.")
public record ProfileRequest(
        @Schema(example = "Backend developer who likes Postgres.")
        String bio) implements StrictRequestBody {
}
