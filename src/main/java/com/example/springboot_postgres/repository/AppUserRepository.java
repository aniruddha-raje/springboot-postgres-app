package com.example.springboot_postgres.repository;

import com.example.springboot_postgres.model.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    // Associations are LAZY by default; these finders declare an entity graph so
    // the profile, posts and roles are fetched up-front for exactly the queries
    // the API serializes. This avoids N+1 selects and LazyInitializationException
    // without forcing every query to load the whole object graph.
    @Override
    @EntityGraph(attributePaths = {"profile", "posts", "roles"})
    List<AppUser> findAll();

    @Override
    @EntityGraph(attributePaths = {"profile", "posts", "roles"})
    Optional<AppUser> findById(Long id);
}
