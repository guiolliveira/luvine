package com.javacore.spring_api_luvine.product.infrastructure.repository;

import com.javacore.spring_api_luvine.product.domain.entity.Category;
import com.javacore.spring_api_luvine.product.domain.valueObject.CategoryName;
import com.javacore.spring_api_luvine.product.domain.valueObject.Slug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    Optional<Category> findByPublicIdAndActiveTrue(UUID publicId);

    Optional<Category> findByPublicId(UUID publicId);

    Optional<Category> findBySlug(Slug slug);

    Optional<Category> findBySlugAndActiveTrue(Slug slug);

    Page<Category> findAllByActiveTrue(Pageable pageable);

    List<Category> findByParentIsNullAndActiveTrue();

    List<Category> findByParentAndActiveTrue(Category parent);

    boolean existsBySlug(Slug slug);

    boolean existsBySlugAndIdNot(Slug slug, Long id);

    boolean existsByCategoryNameAndIdNot(CategoryName categoryName, Long id);

    boolean existsByCategoryName(CategoryName categoryName);
}