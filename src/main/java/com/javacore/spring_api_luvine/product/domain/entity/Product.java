package com.javacore.spring_api_luvine.product.domain.entity;

import com.javacore.spring_api_luvine.common.exception.exceptions.UnchangedValueException;
import com.javacore.spring_api_luvine.product.domain.valueObject.Description;
import com.javacore.spring_api_luvine.product.domain.valueObject.Money;
import com.javacore.spring_api_luvine.product.domain.valueObject.ProductName;
import com.javacore.spring_api_luvine.product.domain.valueObject.Slug;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "product_name", unique = true, nullable = false))
    private ProductName productName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "slug", nullable = false, unique = true, length = 150))
    private Slug slug;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "description", nullable = false))
    private Description description;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "base_price", nullable = false))
    private Money basePrice;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(nullable = false)
    private String lastUpdatedBy;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductVariant> variants;

    private Product(Category category, ProductName productName, Description description, Money basePrice) {
        this.publicId = UUID.randomUUID();
        this.category = category;
        this.productName = productName;
        this.slug = new Slug(productName.value());
        this.description = description;
        this.basePrice = basePrice;
        this.status = Status.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.variants = new HashSet<>();
    }

    public static Product create(
            Category category, ProductName productName,
            Description description, Money basePrice) {
        return new Product(category, productName, description, basePrice);
    }

    public void addVariant(ProductVariant variant) {
        variants.add(variant);
        variant.assignToProduct(this);
    }

    public void removeVariant(ProductVariant variant) {
        variants.remove(variant);
        variant.unassignToProduct();;
    }

    public void changeCategory(Category newCategory) {
        if (this.category.equals(newCategory)) {
            throw new UnchangedValueException("A categoria do produto não pode ser igual a atual");
        }

        this.category = newCategory;
        touch();
    }

    public void changeProductName(ProductName newProductName) {
        if (this.productName.equals(newProductName)) {
            throw new UnchangedValueException("O produto já possui o nome informado");
        }

        this.productName = newProductName;
        touch();
    }

    public void changeSlug(Slug newSlug) {
        if (this.slug.equals(newSlug)) {
            throw new UnchangedValueException("O protudo já possui o slug informado");
        }

        this.slug = newSlug;
        touch();
    }

    public void changeDescription(Description newDescription) {
        if (this.description.equals(newDescription)) {
            throw new UnchangedValueException("A produto já possui a descrição informada");
        }

        this.description = newDescription;
        touch();
    }

    public void changeBasePrice(Money newBasePrice) {
        if (Objects.equals(basePrice, newBasePrice)) {
            throw new UnchangedValueException("O produto já possui o preço base informado");
        }

        this.basePrice = newBasePrice;
        touch();
    }

    public void changeStatus(Status newStatus) {
        if (this.status.equals(newStatus)) {
            throw new UnchangedValueException("O produto já possui o status informado");
        }

        this.status = newStatus;
        touch();
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }
}