package com.example.springboot_postgres.service;

import com.example.springboot_postgres.helpers.Validation;
import com.example.springboot_postgres.model.AppUser;
import com.example.springboot_postgres.model.Role;
import com.example.springboot_postgres.repository.AppUserRepository;
import com.example.springboot_postgres.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

/**
 * Roles and their assignment to users. AppUser.roles is the owning side of the
 * many-to-many, so user_role rows are only ever written by changing a user's
 * roles; changes to {@link Role#getAppUsers()} are ignored by Hibernate.
 */
@Slf4j
@Service
public class RoleService {

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    AppUserRepository userRepository;

    @LogExecutionTime
    public List<Role> getAllRoles() {
        log.info("inside getAllRoles");
        return roleRepository.findAll();
    }

    @LogExecutionTime
    public Role getRole(Long roleId) {
        log.info("inside getRole");
        return findRole(roleId);
    }

    @LogExecutionTime
    public Role createRole(Role role) {
        log.info("inside createRole");
        Validation.requireText(role.getName(), "name");
        // Ignore any client-supplied id so a create can never overwrite an existing role.
        role.setId(null);
        return roleRepository.save(role);
    }

    /** Partial update: only a non-null name is applied. */
    @LogExecutionTime
    @Transactional
    public Role updateRole(Long roleId, Role changes) {
        log.info("inside updateRole");
        Validation.rejectBlank(changes.getName(), "name");
        Role role = findRole(roleId);
        if (changes.getName() != null) {
            role.setName(changes.getName());
        }
        return role;
    }

    @LogExecutionTime
    @Transactional
    public void deleteRole(Long roleId) {
        log.info("inside deleteRole");
        Role role = findRole(roleId);
        // Role is the inverse side, so deleting it doesn't remove its user_role
        // rows, and the foreign key would block the delete. Unassign it through
        // each user's (owning) roles first; Hibernate flushes those join-table
        // deletes before deleting the role.
        for (AppUser user : role.getAppUsers()) {
            user.getRoles().remove(role);
        }
        roleRepository.delete(role);
    }

    /** Adds the role to the user. Idempotent: assigning a role twice is a no-op. */
    @LogExecutionTime
    @Transactional
    public Set<Role> assignRole(Long userId, Long roleId) {
        log.info("inside assignRole");
        AppUser user = findUser(userId);
        user.getRoles().add(findRole(roleId));
        return user.getRoles();
    }

    /** Removes the role from the user. Idempotent: removing an unassigned role is a no-op. */
    @LogExecutionTime
    @Transactional
    public void unassignRole(Long userId, Long roleId) {
        log.info("inside unassignRole");
        AppUser user = findUser(userId);
        user.getRoles().remove(findRole(roleId));
    }

    private AppUser findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "AppUser not found with id " + userId));
    }

    private Role findRole(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Role not found with id " + roleId));
    }
}
