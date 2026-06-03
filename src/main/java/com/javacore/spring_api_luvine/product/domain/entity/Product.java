package com.javacore.spring_api_luvine.product.domain.entity;

import com.javacore.spring_api_luvine.common.exception.exceptions.UnchangedValueException;
import com.javacore.spring_api_luvine.product.domain.exception.DuplicatedSkuException;
import com.javacore.spring_api_luvine.product.domain.exception.VariantAlreadyExistsException;
import com.javacore.spring_api_luvine.product.domain.exception.VariantNotFoundException;
import com.javacore.spring_api_luvine.product.domain.valueObject.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.*;

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

    @Column(length = 500)
    private String thumbnailUrl;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
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
        this.thumbnailUrl = null;
        this.variants = new HashSet<>();
    }

    public static Product create(
            Category category, ProductName productName,
            Description description, Money basePrice) {
        return new Product(category, productName, description, basePrice);
    }

    public void addVariant(ProductVariant variant) {
        validateDuplicatedVariant(variant);
        validateDuplicatedSku(variant);

        variants.add(variant);
        variant.assignToProduct(this);
    }

    public void removeVariant(ProductVariant variant) {
        variants.remove(variant);
        variant.unassignToProduct();
    }

    public ProductVariant findVariantByPublicId(UUID variantPublicId) {
        return this.variants.stream()
                .filter(variant -> variant.getPublicId().equals(variantPublicId))
                .findFirst()
                .orElseThrow(VariantNotFoundException::new);
    }

    public ProductVariant updateVariantAttributes(UUID variantPublicId, Color newColor, Size newSize) {
        ProductVariant variantToUpdate = findVariantByPublicId(variantPublicId);

        boolean alreadyExists = this.variants.stream()
                .filter(variant -> !variant.getPublicId().equals(variantPublicId))
                .anyMatch(variant -> variant.getColor().value().equalsIgnoreCase(newColor.value()) &&
                        variant.getSize().value().equalsIgnoreCase(newSize.value()));

        if (alreadyExists) {
            throw new VariantAlreadyExistsException();
        }

        variantToUpdate.changeColor(newColor);
        variantToUpdate.changeSize(newSize);
        return variantToUpdate;
    }

    public void changeCategory(Category newCategory) {
        if (this.category.equals(newCategory)) {
            throw new UnchangedValueException("A categoria do produto não pode ser igual a atual");
        }

        this.category = newCategory;
    }

    public void rename(ProductName newProductName) {
        if (this.productName.equals(newProductName)) {
            throw new UnchangedValueException("O produto já possui o nome informado");
        }

        this.productName = newProductName;
        this.slug = new Slug(newProductName.value());
    }

    public void changeDescription(Description newDescription) {
        if (this.description.equals(newDescription)) {
            throw new UnchangedValueException("A produto já possui a descrição informada");
        }

        this.description = newDescription;
    }

    public void changeBasePrice(Money newBasePrice) {
        if (Objects.equals(basePrice, newBasePrice)) {
            throw new UnchangedValueException("O produto já possui o preço base informado");
        }

        this.basePrice = newBasePrice;
    }

    public void changeStatus(Status newStatus) {
        if (this.status.equals(newStatus)) {
            throw new UnchangedValueException("O produto já possui o status informado");
        }

        this.status = newStatus;
    }

    public void changeThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Optional<String> resolveThumbnailUrl() {
        return variants.stream()
                .filter(ProductVariant::isActive)
                .flatMap(v -> v.getImages().stream())
                .filter(ProductImage::isPrimaryImage)
                .findFirst()
                .map(ProductImage::getImageUrl);
    }

    public boolean isInStock() {
        if (variants == null || variants.isEmpty()) return false;

        return variants.stream()
                .filter(ProductVariant::isActive)
                .anyMatch(v -> v.getStockQuantity().value() > 0);
    }

    private void validateDuplicatedVariant(ProductVariant productVariant) {
        boolean alreadyExists = this.variants.stream()
                .anyMatch(variant -> variant.getColor().value().equalsIgnoreCase(productVariant.getColor().value()) &&
                        variant.getSize().value().equalsIgnoreCase(productVariant.getSize().value()));

        if (alreadyExists) {
            throw new VariantAlreadyExistsException();
        }
    }

    private void validateDuplicatedSku(ProductVariant productVariant) {
        boolean duplicatedSku = this.variants.stream()
                .anyMatch(variant -> variant.getSku().value().equalsIgnoreCase(productVariant.getSku().value()));

        if (duplicatedSku) {
            throw new DuplicatedSkuException();
        }
    }
}