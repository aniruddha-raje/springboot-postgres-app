package com.example.springboot_postgres.controller;

import com.example.springboot_postgres.model.Post;
import com.example.springboot_postgres.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Posts stored in the local database. Listing and creating go through the
 * owning user; a single post is addressed by its own id.
 */
@Slf4j
@Tag(name = "Posts", description = "A user's posts (JPA/Postgres)")
@RestController
public class PostController {

    @Autowired
    PostService service;

    @Operation(summary = "List a user's posts")
    @GetMapping("/user/id/{userId}/posts")
    public ResponseEntity<List<Post>> getPostsByUser(@PathVariable Long userId) {
        log.info("getPostsByUser called with userId={}", userId);
        return new ResponseEntity<>(service.getPostsByUser(userId), HttpStatus.OK);
    }

    @Operation(summary = "Create a post for a user")
    @PostMapping("/user/id/{userId}/posts")
    public ResponseEntity<Post> createPost(@PathVariable Long userId, @RequestBody Post post) {
        log.info("createPost called with userId={}", userId);
        return new ResponseEntity<>(service.createPost(userId, post), HttpStatus.CREATED);
    }

    @Operation(summary = "Get a post by id")
    @GetMapping("/post/id/{postId}")
    public ResponseEntity<Post> getPost(@PathVariable Long postId) {
        log.info("getPost called with id={}", postId);
        return new ResponseEntity<>(service.getPost(postId), HttpStatus.OK);
    }

    @Operation(summary = "Partially update a post")
    @PatchMapping("/post/id/{postId}")
    public ResponseEntity<Post> updatePost(@PathVariable Long postId, @RequestBody Post changes) {
        log.info("updatePost called with id={}", postId);
        return new ResponseEntity<>(service.updatePost(postId, changes), HttpStatus.OK);
    }

    @Operation(summary = "Delete a post")
    @DeleteMapping("/post/id/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        log.info("deletePost called with id={}", postId);
        service.deletePost(postId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
