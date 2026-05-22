package com.javacore.spring_api_luvine.product.domain.entity;

import com.javacore.spring_api_luvine.common.exception.exceptions.UnchangedValueException;
import com.javacore.spring_api_luvine.product.domain.exception.InsufficientStockException;
import com.javacore.spring_api_luvine.product.domain.valueObject.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "product_variants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "sku", nullable = false, unique = true, length = 50))
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

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private ProductVariant(Sku sku, Color color, Size size, Money price, StockQuantity stockQuantity) {
        this.publicId = UUID.randomUUID();
        this.sku = sku;
        this.color = color;
        this.size = size;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static ProductVariant create(Sku sku, Color color, Size size, Money price, StockQuantity stockQuantity) {
        return new ProductVariant(sku, color, size, price, stockQuantity);
    }

    void assignToProduct(Product product) {
        this.product = product;
    }

    void unassignToProduct() {
        this.product = null;
    }

    public void changeSku(Sku newSku) {
        if (this.sku.equals(newSku)) {
            throw new UnchangedValueException("A variante já possui o sku informado");
        }

        this.sku = newSku;
        touch();
    }

    public void changeColor(Color newColor) {
        if (this.color.equals(newColor)) {
            throw new UnchangedValueException("A variente já possui a cor informada");
        }

        this.color = newColor;
        touch();
    }

    public void changeSize(Size newSize) {
        if (this.size.equals(newSize)) {
            throw new UnchangedValueException("A variante já possui o tamanho informado");
        }

        this.size = newSize;
        touch();
    }

    public void changePrice(Money newPrice) {
        if (Objects.equals(price, newPrice)) {
            throw new UnchangedValueException("A variante já possui o preço informado");
        }

        this.price = newPrice;
        touch();
    }

    public void increaseStock(StockQuantity quantity) {
        this.stockQuantity = new StockQuantity(this.stockQuantity.value() + quantity.value());
    }

    public void decreaseStock(StockQuantity quantity) {
        if (this.stockQuantity.value() < quantity.value()) {
            throw new InsufficientStockException();
        }

        this.stockQuantity = new StockQuantity(this.stockQuantity.value() - quantity.value());
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