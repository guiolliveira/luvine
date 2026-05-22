package com.javacore.spring_api_luvine.product.domain.entity;

import com.javacore.spring_api_luvine.common.exception.exceptions.UnchangedValueException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "product_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean primaryImage;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private ProductImage(String imageUrl, int displayOrder, boolean primaryImage) {
        this.publicId = UUID.randomUUID();
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
        this.primaryImage = primaryImage;
        this.createdAt = Instant.now();
    }

    void assignToVariant(ProductVariant variant) {
        this.productVariant = variant;
    }

    void unassignToVariant() {
        this.productVariant = null;
    }

    public void changeImageUrl(String newImageUrl) {
        if (this.imageUrl.equals(newImageUrl)) {
            throw new UnchangedValueException("O produto já possui a imagem informada");
        }

        this.imageUrl = newImageUrl;
    }

    public void changeDisplayOrder(int newDisplayOrder) {
        if (Objects.equals(displayOrder, newDisplayOrder)) {
            throw new UnchangedValueException("A nova ordem de exibição não pode ser igual a atual");
        }

        this.displayOrder = newDisplayOrder;
    }

    public void setAsPrimary() {
        this.primaryImage = true;
    }
}