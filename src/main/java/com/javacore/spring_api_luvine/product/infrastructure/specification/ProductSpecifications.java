package com.javacore.spring_api_luvine.product.infrastructure.specification;

import com.javacore.spring_api_luvine.product.domain.entity.Product;
import com.javacore.spring_api_luvine.product.domain.entity.ProductVariant;
import com.javacore.spring_api_luvine.product.domain.entity.Status;
import com.javacore.spring_api_luvine.product.domain.valueObject.Color;
import com.javacore.spring_api_luvine.product.domain.valueObject.Money;
import com.javacore.spring_api_luvine.product.domain.valueObject.ProductName;
import com.javacore.spring_api_luvine.product.domain.valueObject.Size;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class ProductSpecifications {

    private ProductSpecifications() {}

    public static Specification<Product> isVisible() {
        return (root, query, cb) -> cb.equal(root.get("status"), Status.ACTIVE);
    }

    public static Specification<Product> hasCategory(UUID categoryPublicId) {
        return (root, query, cb) -> {
            if (categoryPublicId == null) return null;

            return cb.equal(root.get("category").get("publicId"), categoryPublicId);
        };
    }

    public static Specification<Product> nameContains(ProductName productName) {
        return (root, query, cb) -> {
            if (productName == null || productName.value().isBlank()) return null;

            return cb.like(root.get("productName").get("value"), "%" + productName.value() + "%");
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