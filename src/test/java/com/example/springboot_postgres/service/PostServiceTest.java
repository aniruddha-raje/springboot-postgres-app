package com.example.springboot_postgres.service;

import com.example.springboot_postgres.model.AppUser;
import com.example.springboot_postgres.model.Post;
import com.example.springboot_postgres.repository.AppUserRepository;
import com.example.springboot_postgres.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for {@link PostService}; repositories are mocked. */
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    PostRepository postRepository;

    @Mock
    AppUserRepository userRepository;

    @InjectMocks
    PostService service;

    @Test
    void createPost_linksToUserAndIgnoresClientId() {
        AppUser userRef = new AppUser();
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.getReferenceById(1L)).thenReturn(userRef);
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        Post post = new Post();
        post.setId(42L);  // a client trying to overwrite post 42
        post.setContent("hello");
        Post result = service.createPost(1L, post);

        assertNull(result.getId());
        // Post owns the many-to-one, so the link is set on the post.
        assertSame(userRef, result.getAppUser());
    }

    @Test
    void createPost_whenUserMissing_throws404() {
        when(userRepository.existsById(999L)).thenReturn(false);

        Post post = new Post();
        post.setContent("hello");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createPost(999L, post));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(postRepository, never()).save(any());
    }

    @Test
    void createPost_withoutContent_throws400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createPost(1L, new Post()));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(postRepository, never()).save(any());
    }

    @Test
    void updatePost_withBlankContent_throws400() {
        Post changes = new Post();
        changes.setContent("  ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updatePost(5L, changes));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void getPostsByUser_whenUserMissing_throws404() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> service.getPostsByUser(999L));
        verify(postRepository, never()).findByAppUserId(any());
    }

    @Test
    void updatePost_ignoresNullContent() {
        Post existing = new Post();
        existing.setContent("original");
        when(postRepository.findById(5L)).thenReturn(Optional.of(existing));

        service.updatePost(5L, new Post());

        assertEquals("original", existing.getContent());
    }

    @Test
    void deletePost_whenMissing_throws404() {
        when(postRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> service.deletePost(999L));
        verify(postRepository, never()).deleteById(any());
    }
}
