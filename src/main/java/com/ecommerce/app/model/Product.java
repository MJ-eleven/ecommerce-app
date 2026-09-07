package com.ecommerce.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity")
    private int stockQuantity = 0;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ProductVariant> variants = new ArrayList<>();

    @Column(name = "available_attributes")
    private String availableAttributes;

    @Column(name = "attribute_values")
    private String attributeValues;

    // 🔥 CHAMPS PROMOTION
    @Column(name = "promotion_price")
    private BigDecimal promotionPrice;

    @Column(name = "is_on_promotion")
    private boolean onPromotion = false;

    @Column(name = "promotion_label")
    private String promotionLabel;

    // ============================================================
    // CONSTRUCTEURS
    // ============================================================

    public Product() {}

    public Product(String name, String description, BigDecimal price, int stockQuantity, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.active = true;
        this.onPromotion = false;
    }

    // ============================================================
    // GETTERS ET SETTERS
    // ============================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<ProductVariant> getVariants() { return variants; }
    public void setVariants(List<ProductVariant> variants) { this.variants = variants; }

    public String getAvailableAttributes() { return availableAttributes; }
    public void setAvailableAttributes(String availableAttributes) { this.availableAttributes = availableAttributes; }

    public String getAttributeValues() { return attributeValues; }
    public void setAttributeValues(String attributeValues) { this.attributeValues = attributeValues; }

    // 🔥 GETTERS/SETTERS PROMOTION
    public BigDecimal getPromotionPrice() { return promotionPrice; }
    public void setPromotionPrice(BigDecimal promotionPrice) { this.promotionPrice = promotionPrice; }

    public boolean isOnPromotion() { return onPromotion; }
    public void setOnPromotion(boolean onPromotion) { this.onPromotion = onPromotion; }

    public String getPromotionLabel() { return promotionLabel; }
    public void setPromotionLabel(String promotionLabel) { this.promotionLabel = promotionLabel; }

    // ============================================================
    // MÉTHODES UTILITAIRES
    // ============================================================

    public boolean hasVariants() {
        return variants != null && !variants.isEmpty();
    }

    public BigDecimal getMinPrice() {
        if (!hasVariants()) return price;
        BigDecimal min = null;
        for (ProductVariant variant : variants) {
            if (min == null || variant.getPrice().compareTo(min) < 0) {
                min = variant.getPrice();
            }
        }
        return min != null ? min : price;
    }

    public BigDecimal getMaxPrice() {
        if (!hasVariants()) return price;
        BigDecimal max = null;
        for (ProductVariant variant : variants) {
            if (max == null || variant.getPrice().compareTo(max) > 0) {
                max = variant.getPrice();
            }
        }
        return max != null ? max : price;
    }

    public String getPriceRange() {
        if (!hasVariants()) {
            return price.toString();
        }
        return getMinPrice() + " - " + getMaxPrice();
    }

    // 🔥 PRIX AFFICHÉ (avec promotion)
    public BigDecimal getDisplayPrice() {
        if (onPromotion && promotionPrice != null && promotionPrice.compareTo(BigDecimal.ZERO) > 0) {
            return promotionPrice;
        }
        return price;
    }

    // 🔥 CALCULER LE POURCENTAGE DE RÉDUCTION
    public int getDiscountPercentage() {
        if (!onPromotion || promotionPrice == null || price.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        BigDecimal discount = price.subtract(promotionPrice);
        BigDecimal percentage = discount.multiply(BigDecimal.valueOf(100))
                .divide(price, 0, RoundingMode.HALF_UP);
        return percentage.intValue();
    }
}