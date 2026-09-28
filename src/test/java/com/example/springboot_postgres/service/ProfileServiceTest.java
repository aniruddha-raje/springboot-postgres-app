package com.example.springboot_postgres.service;

import com.example.springboot_postgres.model.AppUser;
import com.example.springboot_postgres.model.Profile;
import com.example.springboot_postgres.repository.AppUserRepository;
import com.example.springboot_postgres.repository.ProfileRepository;
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

/** Unit tests for {@link ProfileService}; repositories are mocked. */
@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    AppUserRepository userRepository;

    @Mock
    ProfileRepository profileRepository;

    @InjectMocks
    ProfileService service;

    private AppUser userWithProfile(Profile profile) {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setProfile(profile);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        return user;
    }

    private Profile profile(String bio) {
        Profile profile = new Profile();
        profile.setId(10L);
        profile.setBio(bio);
        return profile;
    }

    @Test
    void getProfile_whenUserMissing_throws404() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.getProfile(999L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getProfile_whenUserHasNoProfile_throws404() {
        userWithProfile(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.getProfile(1L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void putProfile_whenNoProfile_createsAndLinksIt() {
        AppUser user = userWithProfile(null);
        Profile saved = profile("new bio");
        when(profileRepository.save(any(Profile.class))).thenReturn(saved);

        Profile changes = new Profile();
        changes.setBio("new bio");
        Profile result = service.putProfile(1L, changes);

        assertSame(saved, result);
        // AppUser owns the one-to-one, so the link must be set on the user.
        assertSame(saved, user.getProfile());
    }

    @Test
    void putProfile_whenProfileExists_updatesBioInPlace() {
        Profile existing = profile("old bio");
        userWithProfile(existing);

        Profile changes = new Profile();
        changes.setBio("new bio");
        Profile result = service.putProfile(1L, changes);

        assertSame(existing, result);
        assertEquals("new bio", existing.getBio());
        verify(profileRepository, never()).save(any());
    }

    @Test
    void deleteProfile_clearsLinkThenDeletes() {
        Profile existing = profile("bio");
        AppUser user = userWithProfile(existing);

        service.deleteProfile(1L);

        // app_user.profile_id references the profile, so the link must be cleared.
        assertNull(user.getProfile());
        verify(profileRepository).delete(existing);
    }

    @Test
    void deleteProfile_whenUserHasNoProfile_throws404() {
        userWithProfile(null);

        assertThrows(ResponseStatusException.class, () -> service.deleteProfile(1L));
        verify(profileRepository, never()).delete(any());
    }
}
