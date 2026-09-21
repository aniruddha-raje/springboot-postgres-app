package com.example.springboot_postgres.client;

import com.example.springboot_postgres.dto.Post;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * OpenFeign example: a declarative client for JSONPlaceholder {@code /posts}.
 * Feign generates the implementation at startup from these annotations;
 * {@code url} resolves to e.g. {@code https://jsonplaceholder.typicode.com/posts}.
 */
@FeignClient(name = "jsonPlaceholder", url = "${app.jsonPlaceholderUrl}")
public interface JsonPlaceholderFeignClient {

    /** GET {@code /posts?userId={userId}} */
    @GetMapping
    List<Post> getPostsByUser(@RequestParam("userId") Long userId);
}
