package com.javacore.spring_api_luvine.product.infrastructure.repository;

import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.Status;
import com.javacore.spring_api_luvine.product.domain.valueObject.ProductName;
import com.javacore.spring_api_luvine.product.domain.valueObject.Slug;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByPublicId(UUID publicId);

    @EntityGraph(attributePaths = {
            "category",
            "variants",
            "variants.images"
    })
    Optional<Product> findDetailsByPublicId(UUID publicId);

    @EntityGraph(attributePaths = {
            "category",
            "variants",
            "variants.images"
    })
    Optional<Product> findDetailsByPublicIdAndStatus(UUID publicId, Status status);

    boolean existsBySlug(Slug slug);

    boolean existsByProductName(ProductName productName);

    boolean existsByProductNameAndIdNot(ProductName productName, Long id);

    boolean existsBySlugAndIdNot(Slug slug, Long id);
}