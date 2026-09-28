package com.example.springboot_postgres.service;

import com.example.springboot_postgres.helpers.Validation;
import com.example.springboot_postgres.model.AppUser;
import com.example.springboot_postgres.model.Post;
import com.example.springboot_postgres.model.Role;
import com.example.springboot_postgres.repository.AppUserRepository;
import com.example.springboot_postgres.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class AppService {

    @Autowired
    AppUserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @LogExecutionTime
    public List<AppUser> getAllAppUsers(){
        log.info("inside getAllAppUsers");
        return userRepository.findAll();
    }

    @LogExecutionTime
    public Optional<AppUser> getUser(Long id){
        log.info("inside getUser");
        return userRepository.findById(id);
    }

    /**
     * Creates a user, optionally with a new profile and new posts inline, and
     * links existing roles by id. Client-supplied ids are ignored: save()
     * merges any entity that has an id, so a create could otherwise overwrite
     * an existing user, profile or post.
     */
    @LogExecutionTime
    @Transactional
    public AppUser createUser(AppUser user){
        log.info("inside createUser");
        Validation.requireText(user.getUsername(), "username");
        user.setId(null);
        if (user.getProfile() != null) {
            user.getProfile().setId(null);
        }

        Set<Post> posts = user.getPosts() == null ? new HashSet<>() : user.getPosts();
        for (Post post : posts) {
            Validation.requireText(post.getContent(), "post content");
            post.setId(null);
            post.setAppUser(user);
        }
        user.setPosts(posts);

        // Roles are shared, so they are only linked, never created here.
        // Loading them also gives the response their full data.
        Set<Role> roles = new HashSet<>();
        if (user.getRoles() != null) {
            for (Role ref : user.getRoles()) {
                if (ref.getId() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "role id is required");
                }
                roles.add(roleRepository.findById(ref.getId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Role not found with id " + ref.getId())));
            }
        }
        user.setRoles(roles);

        return userRepository.save(user);
    }

    /**
     * Partial update: only the non-null fields of {@code changes} are applied.
     * Returns an empty Optional when no user exists with the given id.
     */
    @LogExecutionTime
    public Optional<AppUser> updateUser(Long id, AppUser changes){
        log.info("inside updateUser");
        Validation.rejectBlank(changes.getUsername(), "username");
        return userRepository.findById(id).map(existing -> {
            if (changes.getUsername() != null) {
                existing.setUsername(changes.getUsername());
            }
            if (changes.getEmail() != null) {
                existing.setEmail(changes.getEmail());
            }
            return userRepository.save(existing);
        });
    }

    /** Returns {@code false} when no user exists with the given id. */
    @LogExecutionTime
    public boolean deleteUser(Long id){
        log.info("inside deleteUser");
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }

}
