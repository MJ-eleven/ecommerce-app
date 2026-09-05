package com.ecommerce.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String name;

    @Column(name = "attribute_1")
    private String attribute1;

    @Column(name = "attribute_2")
    private String attribute2;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity")
    private int stockQuantity = 0;

    @Column(name = "sku", unique = true)
    private String sku;

    // 🔥 NOUVEAU : Image spécifique à la variante
    @Column(name = "image_url", length = 5000)
    private String imageUrl;

    private boolean active = true;

    public ProductVariant() {}

    public ProductVariant(Product product, String name, String attribute1, String attribute2,
                          BigDecimal price, int stockQuantity, String sku, String imageUrl) {
        this.product = product;
        this.name = name;
        this.attribute1 = attribute1;
        this.attribute2 = attribute2;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.sku = sku;
        this.imageUrl = imageUrl;
        this.active = true;
    }

    // ============================================================
    // GETTERS ET SETTERS
    // ============================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAttribute1() { return attribute1; }
    public void setAttribute1(String attribute1) { this.attribute1 = attribute1; }

    public String getAttribute2() { return attribute2; }
    public void setAttribute2(String attribute2) { this.attribute2 = attribute2; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}