package com.javacore.spring_api_luvine.product.domain.entity;

import com.javacore.spring_api_luvine.common.exception.exceptions.UnchangedValueException;
import com.javacore.spring_api_luvine.product.domain.exception.*;
import com.javacore.spring_api_luvine.product.domain.valueObject.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
@Table(name = "product_variants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Version
    @Column(nullable = false)
    private Long version;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(
            name = "sku", nullable = false, unique = true, length = 50, updatable = false))
    private Sku sku;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "color", nullable = false, length = 50))
    private Color color;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "size", nullable = false, length = 20))
    private Size size;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "price", nullable = false))
    private Money price;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "stock_quantity", nullable = false))
    private StockQuantity stockQuantity;

    @Column(nullable = false)
    private boolean active;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ProductImage> images;

    private ProductVariant(Color color, Size size, Money price, StockQuantity stockQuantity) {
        this.publicId = UUID.randomUUID();
        this.sku = new Sku(Sku.generate());
        this.color = color;
        this.size = size;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.active = true;
        this.images = new ArrayList<>();
    }

    public static ProductVariant create(Color color, Size size, Money price, StockQuantity stockQuantity) {
        return new ProductVariant(color, size, price, stockQuantity);
    }

    void assignToProduct(Product product) {
        this.product = product;
    }

    void unassignToProduct() {
        this.product = null;
    }

    public void addImage(ProductImage newImage, Integer displayOrder) {
        Objects.requireNonNull(newImage);

        if (this.images.size() >= 10) {
            throw new ImageLimitExceededException();
        }

        int finalOrder = displayOrder != null ? displayOrder : nextDisplayOrder();

        if (finalOrder < 1 || finalOrder > nextDisplayOrder()) {
            throw new InvalidImageReorderException();
        }

        this.images.stream()
                .filter(image -> image.getDisplayOrder() >= finalOrder)
                .forEach(image -> image.changeDisplayOrder(image.getDisplayOrder() + 1));

        newImage.changeDisplayOrder(finalOrder);

        if (newImage.isPrimaryImage()) {
            this.images.forEach(ProductImage::unsetAsPrimary);
        }

        if (this.images.isEmpty()) {
            newImage.setAsPrimary();
        }

        newImage.assignToVariant(this);

        this.images.add(newImage);

        this.images.sort(
                Comparator.comparing(ProductImage::getDisplayOrder)
        );
    }

    public void removeImage(ProductImage image) {
        Objects.requireNonNull(image);

        if (!this.images.contains(image)) {
            throw new ImageNotFoundException();
        }

        boolean isPrimary = image.isPrimaryImage();

        this.images.remove(image);
        image.unassignToVariant();

        if (isPrimary && !this.images.isEmpty()) {
            this.images.getFirst().setAsPrimary();
        }

        normalizeDisplayOrder();
    }

    public void setPrimary(ProductImage image) {
        Objects.requireNonNull(image);

        if (!this.images.contains(image)) {
            throw new ImageNotFoundException();
        }

        if (image.isPrimaryImage()) {
            throw new ImageAlreadyPrimaryException();
        }

        this.images.forEach(ProductImage::unsetAsPrimary);

        image.setAsPrimary();
    }

    public void reorderImages(List<UUID> orderedImageIds) {
        Objects.requireNonNull(orderedImageIds);

        if (orderedImageIds.isEmpty()) {
            throw new InvalidImageReorderException();
        }

        Set<UUID> uniqueIds = new HashSet<>(orderedImageIds);

        if (uniqueIds.size() != orderedImageIds.size()) {
            throw new InvalidImageReorderException();
        }

        if (orderedImageIds.size() != this.images.size()) {
            throw new InvalidImageReorderException();
        }

        Map<UUID, ProductImage> imageById = this.images.stream()
                .collect(Collectors.toMap(
                        ProductImage::getPublicId,
                        Function.identity()
                ));

        for (int i = 0; i < orderedImageIds.size(); i++) {
            UUID imageId = orderedImageIds.get(i);

            ProductImage image = Optional.ofNullable(imageById.get(imageId))
                    .orElseThrow(InvalidImageReorderException::new);

            image.changeDisplayOrder(i + 1);
        }

        this.images.sort(
                Comparator.comparing(ProductImage::getDisplayOrder)
        );
    }

    public ProductImage getPrimaryImage() {
        return this.images.stream()
                .filter(ProductImage::isPrimaryImage)
                .findFirst()
                .orElseThrow(ImageNotFoundException::new);
    }

    public int nextDisplayOrder() {
        return this.images.stream()
                .map(ProductImage::getDisplayOrder)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    public void normalizeDisplayOrder() {
        for (int i = 0; i < this.images.size(); i++) {
            this.images.get(i).changeDisplayOrder(i + 1);
        }
    }

    public ProductImage findImageByPublicId(UUID imagePublicId) {
        return this.images.stream()
                .filter(image -> image.getPublicId().equals(imagePublicId))
                .findFirst()
                .orElseThrow(ImageNotFoundException::new);
    }

    public void changeColor(Color newColor) {
        if (this.color.equals(newColor)) {
            throw new UnchangedValueException("A variente já possui a cor informada");
        }

        this.color = newColor;
    }

    public void changeSize(Size newSize) {
        if (this.size.equals(newSize)) {
            throw new UnchangedValueException("A variante já possui o tamanho informado");
        }

        this.size = newSize;
    }

    public void changePrice(Money newPrice) {
        if (Objects.equals(price, newPrice)) {
            throw new UnchangedValueException("A variante já possui o preço informado");
        }

        this.price = newPrice;
    }

    public void increaseStock(StockQuantity quantity) {
        this.stockQuantity = new StockQuantity(this.stockQuantity.value() + quantity.value());
    }

    public boolean hasStock(int quantity) {
        return this.stockQuantity.value() >= quantity;
    }

    public void decreaseStock(StockQuantity quantity) {
        if (!hasStock(quantity.value())) {
            throw new InsufficientStockException();
        }

        this.stockQuantity = new StockQuantity(this.stockQuantity.value() - quantity.value());
    }

    public void activate() {
        if (this.active) {
            throw new VariantAlreadyActiveException();
        }

        this.active = true;
    }

    public void deactivate() {
        if (!this.active) {
            throw new VariantAlreadyDisableException();
        }

        this.active = false;
    }
}