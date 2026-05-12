package com.javacore.spring_api_luvine.user.repository;

import com.javacore.spring_api_luvine.user.domain.entity.Address;
import com.javacore.spring_api_luvine.user.domain.valueObject.Cep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findFirstByUserPublicIdAndActiveTrueOrderByCreatedAtDesc(UUID userPublicId);
    Optional<Address> findByPublicIdAndUserPublicId(UUID addressPublicId, UUID userPublicId);

    boolean existsByUserPublicIdAndActiveTrue(UUID userPublicId);
    boolean existsByUserPublicIdAndCepAndNumberAndComplementAndActiveTrue(
            UUID userPublicId, Cep cep, String number, String complement
    );
    List<Address> findAllByUserPublicIdAndActiveTrue(UUID userPublicId);

    @Modifying
    @Query("UPDATE Address a SET a.defaultAddress = FALSE WHERE" +
            " a.user.publicId = :userPublicId AND a.defaultAddress = TRUE")
    void resetDefaultAddressForUser(@Param("userPublicId") UUID userPublicId);
}