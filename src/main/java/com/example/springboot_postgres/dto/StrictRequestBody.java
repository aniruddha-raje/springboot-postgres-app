package com.example.springboot_postgres.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;

/**
 * Makes a request body reject any field it doesn't declare, such as "id",
 * with HTTP 400. Spring Boot configures Jackson to ignore unknown fields
 * globally, so without this a client could send an id and never learn that
 * it was dropped.
 */
public interface StrictRequestBody {

    // Jackson calls this for every field the record doesn't declare. The
    // exception fails deserialization, which Spring MVC reports as a 400.
    @JsonAnySetter
    default void rejectUnknownField(String name, Object value) {
        throw new IllegalArgumentException("Unknown field '" + name + "'");
    }
}
