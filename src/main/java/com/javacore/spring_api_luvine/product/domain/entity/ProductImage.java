package com.javacore.spring_api_luvine.product.domain.entity;

import com.javacore.spring_api_luvine.common.exception.exceptions.UnchangedValueException;
import com.javacore.spring_api_luvine.product.domain.exception.ImageAlreadyPrimaryException;
import com.javacore.spring_api_luvine.product.domain.exception.ImageNotPrimaryException;
import com.javacore.spring_api_luvine.product.domain.valueObject.AltText;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "product_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
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

    @Column(nullable = false, updatable = false)
    private String storageKey;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "alt_text", nullable = false, length = 150))
    private AltText altText;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean primaryImage;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private ProductImage(
            String imageUrl, String storageKey, AltText altText,
            int displayOrder, boolean primaryImage) {
        this.publicId = UUID.randomUUID();
        this.imageUrl = imageUrl;
        this.altText = altText;
        this.storageKey = storageKey;
        this.displayOrder = displayOrder;
        this.primaryImage = primaryImage;
    }

    public static ProductImage create(
            String imageUrl, String storageKey, AltText altText,
            int displayOrder, boolean primaryImage) {
        return new ProductImage(imageUrl, storageKey, altText, displayOrder, primaryImage);
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

    public void changeAltText(AltText newAltText) {
        if (this.altText.equals(newAltText)) {
            throw new UnchangedValueException("A imagem já possui esse texto alternativo");
        }

        this.altText = newAltText;
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

    public void unsetAsPrimary() {
        if (!this.primaryImage) {
            throw new ImageNotPrimaryException();
        }

        this.primaryImage = false;
    }
}