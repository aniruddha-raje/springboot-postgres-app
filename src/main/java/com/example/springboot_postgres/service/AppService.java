package com.example.springboot_postgres.service;

import com.example.springboot_postgres.model.AppUser;
import com.example.springboot_postgres.repository.AppUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AppService {

    @Autowired
    AppUserRepository userRepository;

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

    @LogExecutionTime
    public AppUser createUser(AppUser user){
        log.info("inside createUser");
        return userRepository.save(user);
    }

    /**
     * Partial update: only the non-null fields of {@code changes} are applied.
     * Returns an empty Optional when no user exists with the given id.
     */
    @LogExecutionTime
    public Optional<AppUser> updateUser(Long id, AppUser changes){
        log.info("inside updateUser");
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
