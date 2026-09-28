package com.example.springboot_postgres.service;

import com.example.springboot_postgres.model.AppUser;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for {@link RoleService}; repositories are mocked. */
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    RoleRepository roleRepository;

    @Mock
    AppUserRepository userRepository;

    @InjectMocks
    RoleService service;

    private Role role(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        when(roleRepository.findById(id)).thenReturn(Optional.of(role));
        return role;
    }

    private AppUser user(Long id) {
        AppUser user = new AppUser();
        user.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        return user;
    }

    /** Links both sides in memory, the way Hibernate would load them. */
    private void link(AppUser user, Role role) {
        user.getRoles().add(role);
        role.getAppUsers().add(user);
    }

    @Test
    void deleteRole_unassignsFromEveryUserThenDeletes() {
        Role admin = role(1L, "ADMIN");
        AppUser alice = new AppUser();
        AppUser bob = new AppUser();
        link(alice, admin);
        link(bob, admin);

        service.deleteRole(1L);

        // Removal must happen on the owning side (AppUser.roles) to delete user_role rows.
        assertTrue(alice.getRoles().isEmpty());
        assertTrue(bob.getRoles().isEmpty());
        verify(roleRepository).delete(admin);
    }

    @Test
    void deleteRole_whenMissing_throws404() {
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.deleteRole(999L));
        verify(roleRepository, never()).delete(any());
    }

    @Test
    void assignRole_addsToOwningSide() {
        AppUser user = user(1L);
        Role role = role(2L, "USER");

        Set<Role> roles = service.assignRole(1L, 2L);

        assertTrue(user.getRoles().contains(role));
        assertEquals(Set.of(role), roles);
    }

    @Test
    void assignRole_twice_isIdempotent() {
        user(1L);
        role(2L, "USER");

        service.assignRole(1L, 2L);
        Set<Role> roles = service.assignRole(1L, 2L);

        assertEquals(1, roles.size());
    }

    @Test
    void unassignRole_removesFromOwningSide() {
        AppUser user = user(1L);
        Role role = role(2L, "USER");
        link(user, role);

        service.unassignRole(1L, 2L);

        assertTrue(user.getRoles().isEmpty());
    }

    @Test
    void createRole_ignoresClientId() {
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

        Role role = new Role();
        role.setId(7L);  // a client trying to overwrite role 7
        role.setName("EDITOR");

        assertNull(service.createRole(role).getId());
    }

    @Test
    void createRole_withoutName_throws400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createRole(new Role()));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(roleRepository, never()).save(any());
    }

    @Test
    void updateRole_withBlankName_throws400() {
        Role changes = new Role();
        changes.setName("");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateRole(1L, changes));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}
