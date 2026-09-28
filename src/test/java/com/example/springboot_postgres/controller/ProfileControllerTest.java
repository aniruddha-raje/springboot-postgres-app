package com.example.springboot_postgres.controller;

import com.example.springboot_postgres.model.Profile;
import com.example.springboot_postgres.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Web-layer unit tests for {@link ProfileController}; {@link ProfileService} is mocked. */
@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService service;

    private Profile newProfile(Long id, String bio) {
        Profile profile = new Profile();
        profile.setId(id);
        profile.setBio(bio);
        return profile;
    }

    @Test
    void getProfile_returnsProfile() throws Exception {
        when(service.getProfile(1L)).thenReturn(newProfile(10L, "hello"));

        mockMvc.perform(get("/user/id/1/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.bio").value("hello"));
    }

    @Test
    void getProfile_whenMissing_returns404() throws Exception {
        when(service.getProfile(999L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/user/id/999/profile"))
                .andExpect(status().isNotFound());
    }

    @Test
    void putProfile_returns200AndBody() throws Exception {
        when(service.putProfile(eq(1L), any(Profile.class))).thenReturn(newProfile(10L, "new bio"));

        mockMvc.perform(put("/user/id/1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bio\":\"new bio\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("new bio"));

        verify(service).putProfile(eq(1L), any(Profile.class));
    }

    @Test
    void deleteProfile_returns204() throws Exception {
        mockMvc.perform(delete("/user/id/1/profile"))
                .andExpect(status().isNoContent());

        verify(service).deleteProfile(1L);
    }

    @Test
    void deleteProfile_whenMissing_returns404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND)).when(service).deleteProfile(999L);

        mockMvc.perform(delete("/user/id/999/profile"))
                .andExpect(status().isNotFound());
    }
}
