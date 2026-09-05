package com.ecommerce.app.model;

public class CartItem {

    private Product product;
    private ProductVariant variant;
    private int quantity;

    public CartItem() {}

    public CartItem(Product product, ProductVariant variant, int quantity) {
        this.product = product;
        this.variant = variant;
        this.quantity = quantity;
    }

    // ============================================================
    // GETTERS ET SETTERS
    // ============================================================

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public ProductVariant getVariant() {
        return variant;
    }

    public void setVariant(ProductVariant variant) {
        this.variant = variant;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // ============================================================
    // MÉTHODES UTILITAIRES
    // ============================================================

    public double getTotalPrice() {
        if (variant != null) {
            return variant.getPrice().doubleValue() * quantity;
        }
        return product.getPrice().doubleValue() * quantity;
    }

    public String getDisplayName() {
        if (variant != null) {
            return product.getName() + " (" + variant.getName() + ")";
        }
        return product.getName();
    }

    public String getImageUrl() {
        if (variant != null && variant.getImageUrl() != null && !variant.getImageUrl().isEmpty()) {
            return variant.getImageUrl();
        }
        return product.getImageUrl();
    }

    public double getUnitPrice() {
        if (variant != null) {
            return variant.getPrice().doubleValue();
        }
        return product.getPrice().doubleValue();
    }
}