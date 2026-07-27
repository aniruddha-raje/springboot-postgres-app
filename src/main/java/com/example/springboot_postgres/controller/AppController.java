package com.example.springboot_postgres.controller;

import com.example.springboot_postgres.model.AppUser;
import com.example.springboot_postgres.service.AppService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
public class AppController {

    @Autowired
    AppService service;

    @GetMapping("/all")
    public ResponseEntity<List<AppUser>> getAllAppUsers() {
        log.info("getAllAppUsers called");
        return new ResponseEntity<>(service.getAllAppUsers(), HttpStatus.OK);
    }

    @GetMapping("/id/{appUserId}")
    public ResponseEntity<AppUser> getAppUserById(@PathVariable String appUserId) {
        log.info("getAppUserById called with id={}", appUserId);
        AppUser appUser = service.getUser(Long.valueOf(appUserId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "AppUser not found with id " + appUserId));
        return new ResponseEntity<>(appUser, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<AppUser> createAppUser(@RequestBody AppUser appUser) {
        log.info("createAppUser called");
        AppUser created = service.createUser(appUser);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PatchMapping("/id/{appUserId}")
    public ResponseEntity<AppUser> updateAppUser(@PathVariable String appUserId,
                                                 @RequestBody AppUser changes) {
        log.info("updateAppUser called with id={}", appUserId);
        AppUser updated = service.updateUser(Long.valueOf(appUserId), changes)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "AppUser not found with id " + appUserId));
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/id/{appUserId}")
    public ResponseEntity<Void> deleteAppUser(@PathVariable String appUserId) {
        log.info("deleteAppUser called with id={}", appUserId);
        if (!service.deleteUser(Long.valueOf(appUserId))) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "AppUser not found with id " + appUserId);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
