package com.example.springboot_postgres.controller;

import com.example.springboot_postgres.dto.Post;
import com.example.springboot_postgres.service.JsonPlaceholderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer unit tests for {@link JsonPlaceholderController}. {@link JsonPlaceholderService}
 * is mocked with {@link MockitoBean}, so the tests never make a real call to
 * JSONPlaceholder — the external dependency is fully isolated.
 */
@WebMvcTest(JsonPlaceholderController.class)
class JsonPlaceholderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JsonPlaceholderService service;

    private Post newPost(Long userId, Long id, String title, String body) {
        return new Post(userId, id, title, body);
    }

    // ------------------------------------------------------------------ GETs

    @Test
    void getAllPosts_returnsList() throws Exception {
        when(service.getAllPosts()).thenReturn(List.of(
                newPost(1L, 1L, "title-1", "body-1"),
                newPost(1L, 2L, "title-2", "body-2")));

        mockMvc.perform(get("/external-integration/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("title-1"));

        verify(service, times(1)).getAllPosts();
    }

    @Test
    void getPost_returnsSinglePost() throws Exception {
        when(service.getPost(1L)).thenReturn(newPost(1L, 1L, "title-1", "body-1"));

        mockMvc.perform(get("/external-integration/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.body").value("body-1"));

        verify(service).getPost(1L);
    }

    @Test
    void getPost_whenServiceReports404_propagatesNotFound() throws Exception {
        when(service.getPost(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id 999"));

        mockMvc.perform(get("/external-integration/posts/999"))
                .andExpect(status().isNotFound());

        verify(service).getPost(999L);
    }

    @Test
    void getPostsByUser_returnsList() throws Exception {
        when(service.getPostsByUser(1L)).thenReturn(List.of(newPost(1L, 1L, "t", "b")));

        mockMvc.perform(get("/external-integration/posts/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1));

        verify(service).getPostsByUser(1L);
    }

    // --------------------------------------------------------------- POST

    @Test
    void createPost_returns201() throws Exception {
        when(service.createPost(any(Post.class))).thenReturn(newPost(1L, 101L, "new title", "new body"));

        mockMvc.perform(post("/external-integration/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"title\":\"new title\",\"body\":\"new body\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(101));

        verify(service, times(1)).createPost(any(Post.class));
    }

    // --------------------------------------------------------------- PUT

    @Test
    void replacePost_returns200() throws Exception {
        Post request = newPost(1L, 1L, "replaced", "replaced body");
        when(service.replacePost(eq(1L), any(Post.class))).thenReturn(request);

        mockMvc.perform(put("/external-integration/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"id\":1,\"title\":\"replaced\",\"body\":\"replaced body\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("replaced"));

        verify(service).replacePost(eq(1L), any(Post.class));
    }

    // --------------------------------------------------------------- PATCH

    @Test
    void patchPost_returns200() throws Exception {
        when(service.patchPost(eq(1L), any())).thenReturn(newPost(1L, 1L, "patched title", "body-1"));

        mockMvc.perform(patch("/external-integration/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"patched title\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("patched title"));

        verify(service).patchPost(eq(1L), any());
    }

    // --------------------------------------------------------------- DELETE

    @Test
    void deletePost_returns204() throws Exception {
        mockMvc.perform(delete("/external-integration/posts/1"))
                .andExpect(status().isNoContent());

        verify(service, times(1)).deletePost(1L);
    }

    @Test
    void deletePost_whenUpstreamFails_returns502() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Upstream JSONPlaceholder error"))
                .when(service).deletePost(500L);

        mockMvc.perform(delete("/external-integration/posts/500"))
                .andExpect(status().isBadGateway());

        verify(service).deletePost(500L);
    }

    // ------------------------------------------------------ read isolation

    @Test
    void getAllPosts_doesNotInvokeAnyWriteOperation() throws Exception {
        when(service.getAllPosts()).thenReturn(List.of());

        mockMvc.perform(get("/external-integration/posts"))
                .andExpect(status().isOk());

        verify(service, never()).createPost(any());
        verify(service, never()).replacePost(anyLong(), any());
        verify(service, never()).patchPost(anyLong(), any());
        verify(service, never()).deletePost(anyLong());
    }
}
