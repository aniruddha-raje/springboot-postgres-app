package com.example.springboot_postgres.controller;

import com.example.springboot_postgres.dto.UserRequest;
import com.example.springboot_postgres.model.AppUser;
import com.example.springboot_postgres.service.AppService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer unit tests for {@link AppController}. The {@link AppService} is
 * mocked with {@link MockitoBean}, so no database is involved and the write
 * operations (POST / PATCH / DELETE) never touch a real repository.
 */
@WebMvcTest(AppController.class)
class AppControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppService service;

    private AppUser newUser(Long id, String username, String email) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }

    // ---------------------------------------------------------------------
    // GET /user/all
    // ---------------------------------------------------------------------

    @Test
    void getAllAppUsers_returnsUserList() throws Exception {
        when(service.getAllAppUsers()).thenReturn(List.of(
                newUser(1L, "john_doe", "john@gmail.com"),
                newUser(2L, "jane_smith", "jane@gmail.com")));

        mockMvc.perform(get("/user/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].username").value("john_doe"))
                .andExpect(jsonPath("$[1].email").value("jane@gmail.com"));

        verify(service, times(1)).getAllAppUsers();
    }

    // ---------------------------------------------------------------------
    // GET /user/id/{id}
    // ---------------------------------------------------------------------

    @Test
    void getAppUserById_whenFound_returnsUser() throws Exception {
        when(service.getUser(1L)).thenReturn(Optional.of(newUser(1L, "john_doe", "john@gmail.com")));

        mockMvc.perform(get("/user/id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("john_doe"));

        verify(service).getUser(1L);
    }

    @Test
    void getAppUserById_whenMissing_returns404() throws Exception {
        when(service.getUser(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/user/id/999"))
                .andExpect(status().isNotFound());

        verify(service).getUser(999L);
    }

    // ---------------------------------------------------------------------
    // POST /user  (write endpoint - service mocked)
    // ---------------------------------------------------------------------

    @Test
    void createAppUser_returns201AndBody() throws Exception {
        AppUser saved = newUser(42L, "new_user", "new@gmail.com");
        when(service.createUser(any(UserRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"new_user\",\"email\":\"new@gmail.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.username").value("new_user"));

        verify(service, times(1)).createUser(any(UserRequest.class));
    }

    // ---------------------------------------------------------------------
    // PATCH /user/id/{id}  (write endpoint - service mocked)
    // ---------------------------------------------------------------------

    @Test
    void updateAppUser_whenFound_returns200AndUpdatedBody() throws Exception {
        AppUser updated = newUser(1L, "john_doe", "updated@gmail.com");
        when(service.updateUser(eq(1L), any(UserRequest.class))).thenReturn(Optional.of(updated));

        mockMvc.perform(patch("/user/id/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"updated@gmail.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@gmail.com"));

        verify(service).updateUser(eq(1L), any(UserRequest.class));
    }

    @Test
    void updateAppUser_whenMissing_returns404() throws Exception {
        when(service.updateUser(eq(999L), any(UserRequest.class))).thenReturn(Optional.empty());

        mockMvc.perform(patch("/user/id/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"updated@gmail.com\"}"))
                .andExpect(status().isNotFound());

        verify(service).updateUser(eq(999L), any(UserRequest.class));
    }

    // ---------------------------------------------------------------------
    // DELETE /user/id/{id}  (write endpoint - service mocked)
    // ---------------------------------------------------------------------

    @Test
    void deleteAppUser_whenFound_returns204() throws Exception {
        when(service.deleteUser(1L)).thenReturn(true);

        mockMvc.perform(delete("/user/id/1"))
                .andExpect(status().isNoContent());

        verify(service, times(1)).deleteUser(1L);
    }

    @Test
    void deleteAppUser_whenMissing_returns404() throws Exception {
        when(service.deleteUser(999L)).thenReturn(false);

        mockMvc.perform(delete("/user/id/999"))
                .andExpect(status().isNotFound());

        verify(service).deleteUser(999L);
    }

    @Test
    void nonNumericId_returns400() throws Exception {
        mockMvc.perform(get("/user/id/abc"))
                .andExpect(status().isBadRequest());

        verify(service, never()).getUser(any());
    }

    @Test
    void getAllAppUsers_doesNotInvokeAnyWriteOperation() throws Exception {
        when(service.getAllAppUsers()).thenReturn(List.of());

        mockMvc.perform(get("/user/all"))
                .andExpect(status().isOk());

        verify(service, never()).createUser(any());
        verify(service, never()).updateUser(any(), any());
        verify(service, never()).deleteUser(any());
    }

    // ---------------------------------------------------------------------
    // Request bodies: the id comes from the server or the path, never the body
    // ---------------------------------------------------------------------

    @Test
    void createAppUser_withIdInBody_returns400() throws Exception {
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"username\":\"new_user\"}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).createUser(any());
    }

    @Test
    void createAppUser_withNestedData_returns400() throws Exception {
        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"new_user\",\"profile\":{\"bio\":\"x\"}}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).createUser(any());
    }

    @Test
    void updateAppUser_withIdInBody_returns400() throws Exception {
        mockMvc.perform(patch("/user/id/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":2,\"email\":\"x@gmail.com\"}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).updateUser(any(), any());
    }
}
