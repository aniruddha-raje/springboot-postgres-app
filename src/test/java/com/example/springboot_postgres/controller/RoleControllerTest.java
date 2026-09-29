package com.example.springboot_postgres.controller;

import com.example.springboot_postgres.dto.RoleRequest;
import com.example.springboot_postgres.model.Role;
import com.example.springboot_postgres.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Web-layer unit tests for {@link RoleController}; {@link RoleService} is mocked. */
@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleService service;

    private Role newRole(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }

    // ------------------------------------------------------------ role CRUD

    @Test
    void getAllRoles_returnsList() throws Exception {
        when(service.getAllRoles()).thenReturn(List.of(newRole(1L, "ADMIN"), newRole(2L, "USER")));

        mockMvc.perform(get("/role/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("ADMIN"));
    }

    @Test
    void getRole_whenMissing_returns404() throws Exception {
        when(service.getRole(999L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/role/id/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRole_returns201AndBody() throws Exception {
        when(service.createRole(any(RoleRequest.class))).thenReturn(newRole(3L, "EDITOR"));

        mockMvc.perform(post("/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"EDITOR\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("EDITOR"));
    }

    @Test
    void updateRole_returns200AndBody() throws Exception {
        when(service.updateRole(eq(3L), any(RoleRequest.class))).thenReturn(newRole(3L, "WRITER"));

        mockMvc.perform(patch("/role/id/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"WRITER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("WRITER"));
    }

    @Test
    void deleteRole_returns204() throws Exception {
        mockMvc.perform(delete("/role/id/3"))
                .andExpect(status().isNoContent());

        verify(service).deleteRole(3L);
    }

    @Test
    void role_doesNotSerializeItsUsers() throws Exception {
        // Role.appUsers is @JsonIgnore'd to break the AppUser <-> Role cycle.
        when(service.getRole(1L)).thenReturn(newRole(1L, "ADMIN"));

        mockMvc.perform(get("/role/id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appUsers").doesNotExist());
    }

    // ------------------------------------------------------ role assignment

    @Test
    void assignRole_returnsUsersRoles() throws Exception {
        when(service.assignRole(1L, 2L)).thenReturn(Set.of(newRole(2L, "USER")));

        mockMvc.perform(put("/user/id/1/roles/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("USER"));
    }

    @Test
    void assignRole_whenMissing_returns404() throws Exception {
        when(service.assignRole(1L, 999L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(put("/user/id/1/roles/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unassignRole_returns204() throws Exception {
        mockMvc.perform(delete("/user/id/1/roles/2"))
                .andExpect(status().isNoContent());

        verify(service).unassignRole(1L, 2L);
    }

    @Test
    void unassignRole_whenMissing_returns404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND)).when(service).unassignRole(999L, 2L);

        mockMvc.perform(delete("/user/id/999/roles/2"))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------------
    // Request bodies: the id comes from the server or the path, never the body
    // ---------------------------------------------------------------------

    @Test
    void createRole_withIdInBody_returns400() throws Exception {
        mockMvc.perform(post("/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"EDITOR\"}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).createRole(any());
    }

    @Test
    void updateRole_withIdInBody_returns400() throws Exception {
        mockMvc.perform(patch("/role/id/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":4,\"name\":\"WRITER\"}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).updateRole(any(), any());
    }
}
