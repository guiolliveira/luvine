package com.javacore.spring_api_luvine.user.domain.entity;

import com.javacore.spring_api_luvine.user.domain.valueObject.Cep;
import com.javacore.spring_api_luvine.user.domain.valueObject.Name;
import com.javacore.spring_api_luvine.user.domain.valueObject.Phone;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "addresses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "firstName", nullable = false, length = 100))
    private Name firstName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "lastName", nullable = false, length = 100))
    private Name lastName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "cep", nullable = false, length = 8))
    private Cep cep;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false, length = 20)
    private String number;

    @Column(length = 100)
    private String complement;

    @Column(nullable = false, length = 100)
    private String neighborhood;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 50)
    private String state;

    @Column(nullable = false, length = 100)
    private String country;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "phone", nullable = false, length = 20))
    private Phone phone;

    @Column(nullable = false)
    private boolean defaultAddress;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean active;

    private Address(
            User user, Name firstName, Name lastName,
            Cep cep, String street, String number,
            String complement, String neighborhood, String city,
            String state, String country, Phone phone,
            boolean defaultAddress) {
        this.publicId = UUID.randomUUID();
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.cep = cep;
        this.street = street != null ? street.trim() : null;
        this.number = number != null ? number.trim() : null;
        this.complement = complement != null ? complement.trim() : null;
        this.neighborhood = neighborhood != null ? neighborhood.trim() : null;
        this.city = city != null ? city.trim() : null;
        this.state = state != null ? state.trim() : null;
        this.country = country != null ? country.trim() : null;
        this.phone = phone;
        this.defaultAddress = defaultAddress;
        this.createdAt = Instant.now();
        this.active = true;
    }

    public static Address create(
            User user, Name firstName, Name lastName,
            Cep cep, String street, String number,
            String complement, String neighborhood, String city,
            String state, String country, Phone phone,
            boolean defaultAddress) {
        return new Address(user, firstName, lastName, cep, street, number,
                complement, neighborhood, city, state, country, phone, defaultAddress);
    }

    public void markAsDefault() {
        this.defaultAddress = true;
    }

    public void disable() {
        this.defaultAddress = false;
        this.active = false;
    }
}