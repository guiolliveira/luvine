package com.javacore.spring_api_luvine.product.infrastructure.specification;

import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.valueObject.CategoryName;
import org.springframework.data.jpa.domain.Specification;

public class CategorySpecifications {

    public static Specification<Category> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<Category> hasActive(Boolean active) {
        return (root, query, cb) -> {
            if (active == null) return null;

            return cb.equal(root.get("active"), active);
        };
    }

    public static Specification<Category> nameContains(CategoryName categoryName) {
        return (root, query, cb) -> {
            if (categoryName == null || categoryName.value().isBlank()) return null;

            return cb.like(cb.lower(root.get("categoryName").get("value")), "%" + categoryName.value() + "%");
        };
    }

    public static Specification<Category> hasParent(Category parent) {
        return (root, query, cb) -> {
            if (parent == null) return null;

            return cb.equal(root.get("parent"), parent);
        };
    }

    public static Specification<Category> isRootCategory() {
        return (root, query, cb) -> cb.isNull(root.get("parent"));
    }
}