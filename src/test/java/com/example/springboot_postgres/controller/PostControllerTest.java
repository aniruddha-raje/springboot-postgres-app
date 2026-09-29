package com.example.springboot_postgres.controller;

import com.example.springboot_postgres.dto.PostRequest;
import com.example.springboot_postgres.model.Post;
import com.example.springboot_postgres.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Web-layer unit tests for {@link PostController} (local posts); {@link PostService} is mocked. */
@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService service;

    private Post newPost(Long id, String content) {
        Post post = new Post();
        post.setId(id);
        post.setContent(content);
        return post;
    }

    @Test
    void getPostsByUser_returnsList() throws Exception {
        when(service.getPostsByUser(1L)).thenReturn(List.of(newPost(1L, "first"), newPost(2L, "second")));

        mockMvc.perform(get("/user/id/1/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].content").value("first"));
    }

    @Test
    void getPostsByUser_whenUserMissing_returns404() throws Exception {
        when(service.getPostsByUser(999L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/user/id/999/posts"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPost_returns201AndBody() throws Exception {
        when(service.createPost(eq(1L), any(PostRequest.class))).thenReturn(newPost(5L, "hello"));

        mockMvc.perform(post("/user/id/1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.content").value("hello"));

        verify(service).createPost(eq(1L), any(PostRequest.class));
    }

    @Test
    void getPost_returnsPost() throws Exception {
        when(service.getPost(5L)).thenReturn(newPost(5L, "hello"));

        mockMvc.perform(get("/post/id/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("hello"));
    }

    @Test
    void getPost_whenMissing_returns404() throws Exception {
        when(service.getPost(999L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/post/id/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePost_returns200AndBody() throws Exception {
        when(service.updatePost(eq(5L), any(PostRequest.class))).thenReturn(newPost(5L, "edited"));

        mockMvc.perform(patch("/post/id/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"edited\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("edited"));
    }

    @Test
    void deletePost_returns204() throws Exception {
        mockMvc.perform(delete("/post/id/5"))
                .andExpect(status().isNoContent());

        verify(service).deletePost(5L);
    }

    @Test
    void deletePost_whenMissing_returns404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND)).when(service).deletePost(999L);

        mockMvc.perform(delete("/post/id/999"))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------------
    // Request bodies: the id comes from the server or the path, never the body
    // ---------------------------------------------------------------------

    @Test
    void createPost_withIdInBody_returns400() throws Exception {
        mockMvc.perform(post("/user/id/1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"content\":\"hello\"}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).createPost(any(), any());
    }

    @Test
    void createPost_withJsonPlaceholderFields_returns400() throws Exception {
        mockMvc.perform(post("/user/id/1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"t\",\"body\":\"b\"}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).createPost(any(), any());
    }

    @Test
    void updatePost_withIdInBody_returns400() throws Exception {
        mockMvc.perform(patch("/post/id/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":6,\"content\":\"edited\"}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).updatePost(any(), any());
    }
}
