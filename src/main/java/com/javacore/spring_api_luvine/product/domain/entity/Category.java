package com.javacore.spring_api_luvine.product.domain.entity;

import com.javacore.spring_api_luvine.product.domain.exception.InvalidCategoryHierarchyException;
import com.javacore.spring_api_luvine.product.domain.valueObject.CategoryName;
import com.javacore.spring_api_luvine.product.domain.valueObject.Description;
import com.javacore.spring_api_luvine.product.domain.valueObject.Slug;
import com.javacore.spring_api_luvine.common.exception.exceptions.UnchangedValueException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @JoinColumn(name = "parent_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Category parent;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "category_name",
            nullable = false, unique = true, length = 100))
    private CategoryName categoryName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "slug", nullable = false, unique = true, length = 150))
    private Slug slug;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "description", nullable = false))
    private Description description;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false, updatable = false)
    private Instant updatedAt;

    private Category(Category parent, CategoryName categoryName, Description description) {
        this.publicId = UUID.randomUUID();
        this.parent = parent;
        this.categoryName = categoryName;
        this.slug = new Slug(categoryName.value());
        this.description = description;
        this.imageUrl = null;
        this.displayOrder = 0;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static Category create(Category parent, CategoryName categoryName, Description description) {
        return new Category(parent, categoryName, description);
    }

    public void changeParent(Category newParent) {
        if (this.equals(newParent)) {
            throw new InvalidCategoryHierarchyException();
        }

        this.parent = newParent;
        touch();
    }

    public void changeCategoryName(CategoryName newCategoryName) {
        if (this.categoryName.equals(newCategoryName)) {
            throw new UnchangedValueException("A categoria já possui o nome informado");
        }

        this.categoryName = newCategoryName;
        touch();
    }

    public void changeSlug(Slug newSlug) {
        if (this.slug.equals(newSlug)) {
            throw new UnchangedValueException("A categoria já possui o slug informado");
        }

        this.slug = newSlug;
        touch();
    }

    public void changeDescription(Description newDescription) {
        if (this.description.equals(newDescription)) {
            throw new UnchangedValueException("A categoria já possui a descrição informada");
        }

        this.description = newDescription;
        touch();
    }

    public void changeImageUrl(String newImageUrl) {
        if (this.imageUrl.equals(newImageUrl)) {
            throw new UnchangedValueException("A categoria já possui está imagem");
        }

        this.imageUrl = newImageUrl;
        touch();
    }

    public void changeDisplayOrder(int newDisplayOrder) {
        if (Objects.equals(displayOrder, newDisplayOrder)) {
            throw new UnchangedValueException("A nova ordem de exibição não pode ser igual a atual");
        }

        this.displayOrder = newDisplayOrder;
        touch();
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }
}