package com.example.springboot_postgres.service;

import com.example.springboot_postgres.helpers.Validation;
import com.example.springboot_postgres.model.Post;
import com.example.springboot_postgres.repository.AppUserRepository;
import com.example.springboot_postgres.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Posts stored in the local database (the JPA {@link Post} entity), not the
 * JSONPlaceholder integration. Post owns the link to its user
 * (post.app_user_id), so it is set through {@link Post#setAppUser}.
 */
@Slf4j
@Service
public class PostService {

    @Autowired
    PostRepository postRepository;

    @Autowired
    AppUserRepository userRepository;

    @LogExecutionTime
    public List<Post> getPostsByUser(Long userId) {
        log.info("inside getPostsByUser");
        requireUser(userId);
        return postRepository.findByAppUserId(userId);
    }

    @LogExecutionTime
    @Transactional
    public Post createPost(Long userId, Post post) {
        log.info("inside createPost");
        Validation.requireText(post.getContent(), "content");
        requireUser(userId);
        // Ignore any client-supplied id so a create can never overwrite an existing post.
        post.setId(null);
        // A reference is enough to set the foreign key; no need to load the user.
        post.setAppUser(userRepository.getReferenceById(userId));
        return postRepository.save(post);
    }

    @LogExecutionTime
    public Post getPost(Long postId) {
        log.info("inside getPost");
        return findPost(postId);
    }

    /** Partial update: only a non-null content is applied. */
    @LogExecutionTime
    @Transactional
    public Post updatePost(Long postId, Post changes) {
        log.info("inside updatePost");
        Validation.rejectBlank(changes.getContent(), "content");
        Post post = findPost(postId);
        if (changes.getContent() != null) {
            post.setContent(changes.getContent());
        }
        return post;
    }

    @LogExecutionTime
    public void deletePost(Long postId) {
        log.info("inside deletePost");
        if (!postRepository.existsById(postId)) {
            throw noPost(postId);
        }
        postRepository.deleteById(postId);
    }

    private void requireUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "AppUser not found with id " + userId);
        }
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> noPost(postId));
    }

    private ResponseStatusException noPost(Long postId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id " + postId);
    }
}
