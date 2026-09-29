package com.example.springboot_postgres.controller;

import com.example.springboot_postgres.config.OpenApiConfig;
import com.example.springboot_postgres.dto.RoleRequest;
import com.example.springboot_postgres.model.Role;
import com.example.springboot_postgres.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/** Roles are shared across users, so they have their own CRUD plus assignment endpoints. */
@Slf4j
@Tag(name = "Roles", description = "Roles and role assignment (JPA/Postgres)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
public class RoleController {

    @Autowired
    RoleService service;

    @Operation(summary = "List all roles")
    @GetMapping("/role/all")
    public ResponseEntity<List<Role>> getAllRoles() {
        log.info("getAllRoles called");
        return new ResponseEntity<>(service.getAllRoles(), HttpStatus.OK);
    }

    @Operation(summary = "Get a role by id")
    @GetMapping("/role/id/{roleId}")
    public ResponseEntity<Role> getRole(@PathVariable Long roleId) {
        log.info("getRole called with id={}", roleId);
        return new ResponseEntity<>(service.getRole(roleId), HttpStatus.OK);
    }

    @Operation(summary = "Create a role")
    @PostMapping("/role")
    public ResponseEntity<Role> createRole(@RequestBody RoleRequest request) {
        log.info("createRole called");
        return new ResponseEntity<>(service.createRole(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Partially update a role")
    @PatchMapping("/role/id/{roleId}")
    public ResponseEntity<Role> updateRole(@PathVariable Long roleId, @RequestBody RoleRequest changes) {
        log.info("updateRole called with id={}", roleId);
        return new ResponseEntity<>(service.updateRole(roleId, changes), HttpStatus.OK);
    }

    @Operation(summary = "Delete a role (unassigns it from all users first)")
    @DeleteMapping("/role/id/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        log.info("deleteRole called with id={}", roleId);
        service.deleteRole(roleId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Assign a role to a user")
    @PutMapping("/user/id/{userId}/roles/{roleId}")
    public ResponseEntity<Set<Role>> assignRole(@PathVariable Long userId, @PathVariable Long roleId) {
        log.info("assignRole called with userId={}, roleId={}", userId, roleId);
        return new ResponseEntity<>(service.assignRole(userId, roleId), HttpStatus.OK);
    }

    @Operation(summary = "Remove a role from a user")
    @DeleteMapping("/user/id/{userId}/roles/{roleId}")
    public ResponseEntity<Void> unassignRole(@PathVariable Long userId, @PathVariable Long roleId) {
        log.info("unassignRole called with userId={}, roleId={}", userId, roleId);
        service.unassignRole(userId, roleId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
