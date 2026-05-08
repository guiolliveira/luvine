package com.javacore.spring_api_luvine.user.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @Column(nullable = false, unique = true, updatable = false)
    private String email;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false)
    private boolean active;

    private User(String email, String firstName, String lastName, String password) {
        this.publicId = UUID.randomUUID();
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.active = true;
    }

    public static User create(String email, String firstName, String lastName, String password) {
        return new User(email, firstName, lastName, password);
    }
}