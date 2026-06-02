package com.javacore.spring_api_luvine.product.domain.entity;

import com.javacore.spring_api_luvine.common.exception.exceptions.UnchangedValueException;
import com.javacore.spring_api_luvine.product.domain.valueObject.AltText;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "category_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CategoryImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 500, updatable = false)
    private String imageUrl;

    @Column(nullable = false, updatable = false)
    private String storageKey;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "alt_text", nullable = false, length = 150))
    private AltText altText;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private CategoryImage(String imageUrl, String storageKey, AltText altText) {
        this.publicId = UUID.randomUUID();
        this.imageUrl = imageUrl;
        this.storageKey = storageKey;
        this.altText = altText;
    }

    public static CategoryImage create(String imageUrl, String storageKey, AltText altText) {
        return new CategoryImage(imageUrl, storageKey, altText);
    }

    public void assignToCategory(Category category) {
        this.category = category;
    }

    public void changeAltText(AltText newAltText) {
        if (this.altText.equals(newAltText)) {
            throw new UnchangedValueException("A imagem já possui esse texto alternativo");
        }

        this.altText = newAltText;
    }
}