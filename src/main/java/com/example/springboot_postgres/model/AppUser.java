package com.example.springboot_postgres.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "app_user")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    // No @JsonManagedReference here: Profile has no back-reference to AppUser
    // (the relationship is one-directional), so there is no serialization cycle
    // to break. A managed reference without a matching @JsonBackReference makes
    // Jackson unable to deserialize AppUser (HTTP 415 on POST/PATCH).
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    // Modeled as a Set (not a List): when the entity graph fetches posts and
    // roles together the SQL join produces a cartesian product, and a List/bag
    // would keep the duplicated rows. A Set de-duplicates by entity identity.
    @JsonManagedReference
    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Post> posts = new HashSet<>();

    // Many-to-many: both sides are collections, so @JsonManagedReference/
    // @JsonBackReference cannot be used (a back-reference must be single-valued).
    // The cycle is broken by @JsonIgnore on the inverse side (Role.appUsers).
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
