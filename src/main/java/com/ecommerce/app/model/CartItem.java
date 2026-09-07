package com.ecommerce.app.model;

public class CartItem {

    private Product product;
    private ProductVariant variant;
    private int quantity;
    private double unitPrice;  // 🔥 NOUVEAU : prix unitaire (avec promotion)

    public CartItem() {}

    public CartItem(Product product, ProductVariant variant, int quantity, double unitPrice) {
        this.product = product;
        this.variant = variant;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
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

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    // ============================================================
    // MÉTHODES UTILITAIRES
    // ============================================================

    public double getTotalPrice() {
        return unitPrice * quantity;
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
}