package com.example.springboot_postgres.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Inverse side of the many-to-many. Ignored during (de)serialization to
    // break the AppUser <-> Role cycle; a Set back-reference is not something
    // Jackson's @JsonBackReference can handle.
    @JsonIgnore
    @ManyToMany(mappedBy = "roles")
    private Set<AppUser> appUsers = new HashSet<>();
}
