package com.example.springboot_postgres.helpers;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Request-body checks shared by the services; failures surface as HTTP 400. */
public final class Validation {

    private Validation() {
    }

    /** For creates: a required text field must be present and not blank. */
    public static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
    }

    /** For partial updates: null means "leave unchanged", but blank is rejected. */
    public static void rejectBlank(String value, String field) {
        if (value != null && value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " must not be blank");
        }
    }
}
