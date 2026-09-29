package com.example.springboot_postgres.controller;

import com.example.springboot_postgres.config.OpenApiConfig;
import com.example.springboot_postgres.dto.Post;
import com.example.springboot_postgres.service.JsonPlaceholderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Demonstrates outbound integration with the JSONPlaceholder {@code /posts}
 * API. All calls are delegated to {@link JsonPlaceholderService}.
 */
@Slf4j
@Tag(name = "JSONPlaceholder", description = "External JSONPlaceholder /posts integration")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/external-integration/posts")
public class JsonPlaceholderController {

    @Autowired
    private JsonPlaceholderService jsonPlaceholderService;

    @Operation(summary = "List all posts")
    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        log.info("getAllPosts called");
        return new ResponseEntity<>(jsonPlaceholderService.getAllPosts(), HttpStatus.OK);
    }

    @Operation(summary = "Get a post by id")
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPost(@PathVariable Long id) {
        log.info("getPost called with id={}", id);
        return new ResponseEntity<>(jsonPlaceholderService.getPost(id), HttpStatus.OK);
    }

    @Operation(summary = "List posts by user id")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> getPostsByUser(@PathVariable Long userId) {
        log.info("getPostsByUser called with userId={}", userId);
        return new ResponseEntity<>(jsonPlaceholderService.getPostsByUser(userId), HttpStatus.OK);
    }

    @Operation(summary = "Create a post")
    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        log.info("createPost called");
        return new ResponseEntity<>(jsonPlaceholderService.createPost(post), HttpStatus.CREATED);
    }

    @Operation(summary = "Replace a post")
    @PutMapping("/{id}")
    public ResponseEntity<Post> replacePost(@PathVariable Long id, @RequestBody Post post) {
        log.info("replacePost called with id={}", id);
        return new ResponseEntity<>(jsonPlaceholderService.replacePost(id, post), HttpStatus.OK);
    }

    @Operation(summary = "Partially update a post")
    @PatchMapping("/{id}")
    public ResponseEntity<Post> patchPost(@PathVariable Long id, @RequestBody Map<String, Object> changes) {
        log.info("patchPost called with id={}", id);
        return new ResponseEntity<>(jsonPlaceholderService.patchPost(id, changes), HttpStatus.OK);
    }

    @Operation(summary = "Delete a post")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        log.info("deletePost called with id={}", id);
        jsonPlaceholderService.deletePost(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
