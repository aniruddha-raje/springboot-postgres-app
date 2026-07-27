package com.example.springboot_postgres.service;

import com.example.springboot_postgres.config.AppProperties;
import com.example.springboot_postgres.dto.Post;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/**
 * Thin client over the JSONPlaceholder {@code /posts} resource, demonstrating
 * outbound REST integration with {@link RestTemplate}. Upstream 4xx/5xx
 * responses are translated into {@link ResponseStatusException} so they surface
 * to the caller with a sensible HTTP status instead of a raw stack trace.
 */
@Slf4j
@Service
public class JsonPlaceholderService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public JsonPlaceholderService(RestTemplate restTemplate, AppProperties properties) {
        this.restTemplate = restTemplate;
        // e.g. https://jsonplaceholder.typicode.com/posts
        this.baseUrl = properties.getJsonPlaceholderUrl();
    }

    public List<Post> getAllPosts() {
        log.info("fetching all posts");
        return exchangeList(baseUrl);
    }

    public List<Post> getPostsByUser(Long userId) {
        log.info("fetching posts for userId={}", userId);
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("userId", userId)
                .toUriString();
        return exchangeList(url);
    }

    public Post getPost(Long id) {
        log.info("fetching post id={}", id);
        try {
            return restTemplate.getForObject(baseUrl + "/" + id, Post.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id " + id);
        } catch (HttpStatusCodeException e) {
            throw upstream(e);
        }
    }

    public Post createPost(Post post) {
        log.info("creating post");
        try {
            return restTemplate.postForObject(baseUrl, post, Post.class);
        } catch (HttpStatusCodeException e) {
            throw upstream(e);
        }
    }

    /** Full replace of the post with the given id (HTTP PUT). */
    public Post replacePost(Long id, Post post) {
        log.info("replacing post id={}", id);
        try {
            ResponseEntity<Post> response = restTemplate.exchange(
                    baseUrl + "/" + id, HttpMethod.PUT, new HttpEntity<>(post), Post.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id " + id);
        } catch (HttpStatusCodeException e) {
            throw upstream(e);
        }
    }

    /** Partial update of the post with the given id (HTTP PATCH). */
    public Post patchPost(Long id, Map<String, Object> changes) {
        log.info("patching post id={}", id);
        try {
            ResponseEntity<Post> response = restTemplate.exchange(
                    baseUrl + "/" + id, HttpMethod.PATCH, new HttpEntity<>(changes), Post.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id " + id);
        } catch (HttpStatusCodeException e) {
            throw upstream(e);
        }
    }

    public void deletePost(Long id) {
        log.info("deleting post id={}", id);
        try {
            restTemplate.delete(baseUrl + "/" + id);
        } catch (HttpStatusCodeException e) {
            throw upstream(e);
        }
    }

    private List<Post> exchangeList(String url) {
        try {
            ResponseEntity<List<Post>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Post>>() {});
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            throw upstream(e);
        }
    }

    private ResponseStatusException upstream(HttpStatusCodeException e) {
        log.warn("JSONPlaceholder call failed: {} {}", e.getStatusCode(), e.getStatusText());
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "Upstream JSONPlaceholder error: " + e.getStatusCode());
    }
}
