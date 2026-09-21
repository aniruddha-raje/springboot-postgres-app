package com.example.springboot_postgres.service;

import com.example.springboot_postgres.client.JsonPlaceholderFeignClient;
import com.example.springboot_postgres.config.AppProperties;
import com.example.springboot_postgres.dto.Post;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Thin client over the JSONPlaceholder {@code /posts} resource. Most calls use
 * {@link RestClient}; two are kept on other clients as side-by-side examples:
 * <ul>
 *   <li>{@link #getPost} — {@link RestTemplate} (the older, template-style client)</li>
 *   <li>{@link #getPostsByUser} — OpenFeign ({@link JsonPlaceholderFeignClient})</li>
 * </ul>
 * Upstream 4xx/5xx responses are translated into {@link ResponseStatusException}
 * so they surface to the caller with a sensible HTTP status instead of a raw
 * stack trace.
 */
@Slf4j
@Service
public class JsonPlaceholderService {

    private final RestClient restClient;
    private final RestTemplate restTemplate;
    private final JsonPlaceholderFeignClient feignClient;
    private final String baseUrl;

    public JsonPlaceholderService(RestClient restClient,
                                  RestTemplate restTemplate,
                                  JsonPlaceholderFeignClient feignClient,
                                  AppProperties properties) {
        this.restClient = restClient;
        this.restTemplate = restTemplate;
        this.feignClient = feignClient;
        // e.g. https://jsonplaceholder.typicode.com/posts — only RestTemplate
        // needs it; RestClient and Feign are configured with it up-front.
        this.baseUrl = properties.getJsonPlaceholderUrl();
    }

    // ------------------------------------------------------------ RestClient

    public List<Post> getAllPosts() {
        log.info("fetching all posts");
        try {
            return restClient.get()
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Post>>() {});
        } catch (HttpStatusCodeException e) {
            throw upstream(e.getStatusCode().toString());
        }
    }

    public Post createPost(Post post) {
        log.info("creating post");
        try {
            return restClient.post()
                    .body(post)
                    .retrieve()
                    .body(Post.class);
        } catch (HttpStatusCodeException e) {
            throw upstream(e.getStatusCode().toString());
        }
    }

    /** Full replace of the post with the given id (HTTP PUT). */
    public Post replacePost(Long id, Post post) {
        log.info("replacing post id={}", id);
        try {
            return restClient.put()
                    .uri("/{id}", id)
                    .body(post)
                    .retrieve()
                    .body(Post.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw notFound(id);
        } catch (HttpStatusCodeException e) {
            throw upstream(e.getStatusCode().toString());
        }
    }

    /** Partial update of the post with the given id (HTTP PATCH). */
    public Post patchPost(Long id, Map<String, Object> changes) {
        log.info("patching post id={}", id);
        try {
            return restClient.patch()
                    .uri("/{id}", id)
                    .body(changes)
                    .retrieve()
                    .body(Post.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw notFound(id);
        } catch (HttpStatusCodeException e) {
            throw upstream(e.getStatusCode().toString());
        }
    }

    public void deletePost(Long id) {
        log.info("deleting post id={}", id);
        try {
            restClient.delete()
                    .uri("/{id}", id)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpStatusCodeException e) {
            throw upstream(e.getStatusCode().toString());
        }
    }

    // ---------------------------------------------- RestTemplate (example)

    public Post getPost(Long id) {
        log.info("fetching post id={} (RestTemplate)", id);
        try {
            return restTemplate.getForObject(baseUrl + "/{id}", Post.class, id);
        } catch (HttpClientErrorException.NotFound e) {
            throw notFound(id);
        } catch (HttpStatusCodeException e) {
            throw upstream(e.getStatusCode().toString());
        }
    }

    // --------------------------------------------------- OpenFeign (example)

    public List<Post> getPostsByUser(Long userId) {
        log.info("fetching posts for userId={} (OpenFeign)", userId);
        try {
            return feignClient.getPostsByUser(userId);
        } catch (FeignException e) {
            // Feign reports failures with its own exception type, not Spring's.
            // status() is -1 when no response arrived (connection error/timeout).
            throw upstream(e.status() > 0 ? String.valueOf(e.status()) : "no response");
        }
    }

    // --------------------------------------------------------------- helpers

    private ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id " + id);
    }

    private ResponseStatusException upstream(String status) {
        log.warn("JSONPlaceholder call failed: {}", status);
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "Upstream JSONPlaceholder error: " + status);
    }
}
