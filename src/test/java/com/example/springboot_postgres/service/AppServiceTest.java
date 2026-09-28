package com.example.springboot_postgres.service;

import com.example.springboot_postgres.model.AppUser;
import com.example.springboot_postgres.model.Post;
import com.example.springboot_postgres.model.Profile;
import com.example.springboot_postgres.model.Role;
import com.example.springboot_postgres.repository.AppUserRepository;
import com.example.springboot_postgres.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for {@link AppService}; repositories are mocked. */
@ExtendWith(MockitoExtension.class)
class AppServiceTest {

    @Mock
    AppUserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @InjectMocks
    AppService service;

    private AppUser newUser(String username) {
        AppUser user = new AppUser();
        user.setUsername(username);
        return user;
    }

    private void saveReturnsArgument() {
        when(userRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createUser_ignoresAllClientIds() {
        saveReturnsArgument();
        AppUser user = newUser("new_user");
        user.setId(1L);                     // would overwrite user 1
        Profile profile = new Profile();
        profile.setId(1L);                  // would hijack profile 1
        user.setProfile(profile);
        Post post = new Post();
        post.setId(1L);                     // would hijack post 1
        post.setContent("hello");
        user.setPosts(Set.of(post));

        AppUser created = service.createUser(user);

        assertNull(created.getId());
        assertNull(created.getProfile().getId());
        assertNull(post.getId());
    }

    @Test
    void createUser_linksInlinePostsToTheUser() {
        saveReturnsArgument();
        AppUser user = newUser("new_user");
        Post post = new Post();
        post.setContent("hello");
        user.setPosts(Set.of(post));

        service.createUser(user);

        // Post owns the link, so it must point back at the new user.
        assertSame(user, post.getAppUser());
    }

    @Test
    void createUser_linksExistingRolesById() {
        saveReturnsArgument();
        Role stored = new Role();
        stored.setId(1L);
        stored.setName("ADMIN");
        when(roleRepository.findById(1L)).thenReturn(Optional.of(stored));
        Role ref = new Role();
        ref.setId(1L);                      // the client only sends the id
        AppUser user = newUser("new_user");
        user.setRoles(Set.of(ref));

        AppUser created = service.createUser(user);

        // The loaded role replaces the id-only reference, so its name is returned.
        assertEquals(Set.of(stored), created.getRoles());
    }

    @Test
    void createUser_withUnknownRole_throws400() {
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());
        Role ref = new Role();
        ref.setId(999L);
        AppUser user = newUser("new_user");
        user.setRoles(Set.of(ref));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.createUser(user));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_withRoleMissingId_throws400() {
        AppUser user = newUser("new_user");
        user.setRoles(Set.of(new Role()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.createUser(user));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createUser_withoutUsername_throws400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createUser(new AppUser()));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_withPostMissingContent_throws400() {
        AppUser user = newUser("new_user");
        user.setPosts(Set.of(new Post()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.createUser(user));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createUser_withNullCollections_succeeds() {
        saveReturnsArgument();
        AppUser user = newUser("new_user");
        user.setPosts(null);
        user.setRoles(null);

        AppUser created = service.createUser(user);

        assertTrue(created.getPosts().isEmpty());
        assertTrue(created.getRoles().isEmpty());
    }

    @Test
    void updateUser_withBlankUsername_throws400() {
        AppUser changes = newUser(" ");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateUser(1L, changes));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}
