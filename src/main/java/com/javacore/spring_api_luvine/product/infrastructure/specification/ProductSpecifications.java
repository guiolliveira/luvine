package com.javacore.spring_api_luvine.product.infrastructure.specification;

import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.ProductVariant;
import com.javacore.spring_api_luvine.product.domain.entity.Status;
import com.javacore.spring_api_luvine.product.domain.valueObject.*;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {

    private ProductSpecifications() {}

    public static Specification<Product> isVisible() {
        return (root, query, cb) -> cb.equal(root.get("status"), Status.ACTIVE);
    }

    public static Specification<Product> hasStatus(Status status) {
        return (root, query, cb) -> {
            if (status == null) return null;

            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Product> hasCategory(Slug categorySlug) {
        return (root, query, cb) -> {
            if (categorySlug == null || categorySlug.value().isBlank()) return null;

            return cb.equal(root.get("category").get("slug").get("value"), categorySlug.value());
        };
    }

    public static Specification<Product> nameContains(ProductName productName) {
        return (root, query, cb) -> {
            if (productName == null || productName.value().isBlank()) return null;

            return cb.like(cb.lower(root.get("productName").get("value")), "%" + productName.value() + "%");
        };
    }

    public static Specification<Product> basePriceGreaterThanOrEqualTo(Money minPrice) {
        return (root, query, cb) -> {
            if (minPrice == null || minPrice.value() == null) return null;

            return cb.greaterThanOrEqualTo(root.get("basePrice").get("value"), minPrice.value());
        };
    }

    public static Specification<Product> basePriceLessThanOrEqualTo(Money maxPrice) {
        return (root, query, cb) -> {
            if (maxPrice == null || maxPrice.value() == null) return null;

            return cb.lessThanOrEqualTo(root.get("basePrice").get("value"), maxPrice.value());
        };
    }

    public static Specification<Product> hasColor(Color color) {
        return (root, query, cb) -> {
            if (color == null || color.value().isBlank()) return null;

            Join<Product, ProductVariant> variantJoin = getOrCreateVariantJoin(root, query);

            return cb.equal(variantJoin.get("color").get("value"), color.value());
        };
    }

    public static Specification<Product> hasSize(Size size) {
        return (root, query, cb) -> {
            if (size == null || size.value().isBlank()) return null;

            Join<Product, ProductVariant> variantJoin = getOrCreateVariantJoin(root, query);

            return cb.equal(variantJoin.get("size").get("value"), size.value());
        };
    }

    @SuppressWarnings("unchecked")
    private static Join<Product, ProductVariant> getOrCreateVariantJoin(Root<Product> root, CriteriaQuery<?> query) {
        if (query != null) {
            query.distinct(true);
        }

        return root.getJoins().stream()
                .filter(join -> "variants".equals(join.getAttribute().getName()))
                .map(join -> (Join<Product, ProductVariant>) join)
                .findFirst()
                .orElseGet(() -> root.join("variants"));
    }
}