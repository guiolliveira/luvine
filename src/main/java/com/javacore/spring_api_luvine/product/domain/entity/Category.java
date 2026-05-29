package com.javacore.spring_api_luvine.product.domain.entity;

import com.javacore.spring_api_luvine.product.domain.exception.CategoryAlreadyActivateException;
import com.javacore.spring_api_luvine.product.domain.exception.CategoryAlreadyDeactivateException;
import com.javacore.spring_api_luvine.product.domain.exception.CircularHierarchyDetectedException;
import com.javacore.spring_api_luvine.product.domain.exception.InvalidCategoryHierarchyException;
import com.javacore.spring_api_luvine.product.domain.valueObject.CategoryName;
import com.javacore.spring_api_luvine.product.domain.valueObject.Description;
import com.javacore.spring_api_luvine.product.domain.valueObject.Slug;
import com.javacore.spring_api_luvine.common.exception.exceptions.UnchangedValueException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
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

    @Column(nullable = false)
    private boolean active;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    private Category(Category parent, CategoryName categoryName, Description description) {
        this.publicId = UUID.randomUUID();
        this.parent = parent;
        this.categoryName = categoryName;
        this.slug = new Slug(categoryName.value());
        this.description = description;
        this.active = true;
    }

    public static Category create(Category parent, CategoryName categoryName, Description description) {
        return new Category(parent, categoryName, description);
    }

    public void changeParent(Category newParent) {
        if (newParent == null) {
            this.parent = null;
            return;
        }

        if (this.equals(newParent)) {
            throw new InvalidCategoryHierarchyException();
        }

        validateCircularHierarchy(newParent);

        this.parent = newParent;
    }

    public void rename(CategoryName newCategoryName) {
        if (this.categoryName.equals(newCategoryName)) {
            throw new UnchangedValueException("A categoria já possui o nome informado");
        }

        this.categoryName = newCategoryName;
        this.slug = new Slug(newCategoryName.value());
    }

    public void changeDescription(Description newDescription) {
        if (this.description.equals(newDescription)) {
            throw new UnchangedValueException("A categoria já possui a descrição informada");
        }

        this.description = newDescription;
    }

    public void activate() {
        if (this.active) {
            throw new CategoryAlreadyActivateException();
        }

        this.active = true;
    }

    public void deactivate() {
        if (!this.active) {
            throw new CategoryAlreadyDeactivateException();
        }

        this.active = false;
    }

    private void validateCircularHierarchy(Category newParent) {
        Category current = newParent;

        while (current != null) {
            if (current.equals(this)) {
                throw new CircularHierarchyDetectedException();
            }
            current = current.getParent();
        }
    }
}