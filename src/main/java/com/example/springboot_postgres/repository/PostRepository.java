package com.example.springboot_postgres.repository;

import com.example.springboot_postgres.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // Derived query on the nested property Post.appUser.id
    List<Post> findByAppUserId(Long appUserId);
}
