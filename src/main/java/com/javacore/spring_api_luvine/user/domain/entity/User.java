package com.javacore.spring_api_luvine.user.domain.entity;

import com.javacore.spring_api_luvine.user.domain.valueObject.Email;
import com.javacore.spring_api_luvine.user.domain.valueObject.Name;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false, unique = true))
    private Email email;

    @Column(unique = true, length = 11)
    private String cpf;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "firstName", nullable = false, length = 100))
    private Name firstName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "lastName", nullable = false, length = 100))
    private Name lastName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean emailVerified;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private UserProvider userProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;

    private String avatarUrl;

    private Instant lastVerificationEmailSentAt;

    @Column(nullable = false)
    private Integer verificationEmailRequestCount;

    private User(Email email, Name firstName, Name lastName, String password, UserProvider userProvider) {
        this.publicId = UUID.randomUUID();
        this.email = email;
        this.cpf = null;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.phone = "";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.active = true;
        this.emailVerified = false;
        this.userProvider = userProvider;
        this.userRole = UserRole.CUSTOMER;
        this.avatarUrl = null;
        this.verificationEmailRequestCount = 0;
    }

    public static User create(
            Email email, Name firstName,
            Name lastName, String password,
            UserProvider userProvider) {
        return new User(email, firstName, lastName, password, userProvider);
    }

    public void markEmailAsVerified() {
        this.emailVerified = true;
    }

    public void markVerificationEmailSent() {
        this.lastVerificationEmailSentAt = Instant.now();
        this.verificationEmailRequestCount++;
    }

    public void resetEmailVerificationRequests() {
        this.verificationEmailRequestCount = 0;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return getEmail().value();
    }

    @Override
    public boolean isEnabled() {
        return isActive();
    }
}