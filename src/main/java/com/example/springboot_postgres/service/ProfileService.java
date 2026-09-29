package com.example.springboot_postgres.service;

import com.example.springboot_postgres.dto.ProfileRequest;
import com.example.springboot_postgres.model.AppUser;
import com.example.springboot_postgres.model.Profile;
import com.example.springboot_postgres.repository.AppUserRepository;
import com.example.springboot_postgres.repository.ProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * A user's profile. AppUser owns the one-to-one (app_user.profile_id), so the
 * link is always set or cleared through {@link AppUser#setProfile}.
 */
@Slf4j
@Service
public class ProfileService {

    @Autowired
    AppUserRepository userRepository;

    @Autowired
    ProfileRepository profileRepository;

    @LogExecutionTime
    @Transactional(readOnly = true)
    public Profile getProfile(Long userId) {
        log.info("inside getProfile");
        Profile profile = findUser(userId).getProfile();
        if (profile == null) {
            throw noProfile(userId);
        }
        return profile;
    }

    /** Creates the user's profile, or replaces its bio if one already exists. */
    @LogExecutionTime
    @Transactional
    public Profile putProfile(Long userId, ProfileRequest request) {
        log.info("inside putProfile");
        AppUser user = findUser(userId);
        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile();
            profile.setBio(request.bio());
            profile = profileRepository.save(profile);
            // The user is managed, so the profile_id update is flushed on commit.
            user.setProfile(profile);
        } else {
            profile.setBio(request.bio());
        }
        return profile;
    }

    @LogExecutionTime
    @Transactional
    public void deleteProfile(Long userId) {
        log.info("inside deleteProfile");
        AppUser user = findUser(userId);
        Profile profile = user.getProfile();
        if (profile == null) {
            throw noProfile(userId);
        }
        // app_user.profile_id references the profile row, so clear the link
        // first. Hibernate flushes this UPDATE before the DELETE.
        user.setProfile(null);
        profileRepository.delete(profile);
    }

    private AppUser findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "AppUser not found with id " + userId));
    }

    private ResponseStatusException noProfile(Long userId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found for user " + userId);
    }
}
