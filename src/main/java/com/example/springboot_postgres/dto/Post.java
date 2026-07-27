package com.example.springboot_postgres.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a post from the JSONPlaceholder API
 * (https://jsonplaceholder.typicode.com/posts).
 *
 * <p>This is a DTO for the external integration and is deliberately separate
 * from the JPA {@link com.example.springboot_postgres.model.Post} entity.
 *
 * <p>{@code id} is {@code null} on create requests (the server assigns it) and
 * null fields are omitted when serialised, which keeps PATCH payloads partial.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Post(Long userId, Long id, String title, String body) {
}
