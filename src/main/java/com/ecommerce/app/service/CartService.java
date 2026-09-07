package com.ecommerce.app.service;

import com.ecommerce.app.model.CartItem;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.ProductVariant;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final List<CartItem> cartItems = new ArrayList<>();

    // Ajouter un produit simple (avec promotion)
    public void addToCart(Product product, int quantity) {
        Optional<CartItem> existingItem = cartItems.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId())
                        && item.getVariant() == null)
                .findFirst();

        // 🔥 Utiliser le prix promotion si disponible
        double price = product.isOnPromotion() && product.getPromotionPrice() != null ?
                product.getPromotionPrice().doubleValue() :
                product.getPrice().doubleValue();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
            // 🔥 Mettre à jour le prix si la promotion a changé
            existingItem.get().setUnitPrice(price);
        } else {
            cartItems.add(new CartItem(product, null, quantity, price));
        }
    }

    // Ajouter une variante (avec promotion du produit parent)
    public void addToCart(ProductVariant variant, int quantity) {
        Optional<CartItem> existingItem = cartItems.stream()
                .filter(item -> item.getVariant() != null
                        && item.getVariant().getId().equals(variant.getId()))
                .findFirst();

        // 🔥 Utiliser le prix de la variante (ou promotion du produit parent)
        double price = variant.getPrice().doubleValue();
        Product product = variant.getProduct();
        if (product.isOnPromotion() && product.getPromotionPrice() != null) {
            price = product.getPromotionPrice().doubleValue();
        }

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
            existingItem.get().setUnitPrice(price);
        } else {
            cartItems.add(new CartItem(variant.getProduct(), variant, quantity, price));
        }
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void removeFromCart(Long productId) {
        cartItems.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public void updateQuantity(Long productId, int quantity) {
        cartItems.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));
    }

    public void clearCart() {
        cartItems.clear();
    }

    public int getTotalItems() {
        return cartItems.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public double getTotalPrice() {
        return cartItems.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }
}