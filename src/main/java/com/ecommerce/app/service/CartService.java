package com.ecommerce.app.service;

import com.ecommerce.app.model.CartItem;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.ProductVariant;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@SessionScope
public class CartService {

    private final List<CartItem> cartItems = new ArrayList<>();

    // Ajouter un produit simple
    public void addToCart(Product product, int quantity) {
        Optional<CartItem> existingItem = cartItems.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId())
                        && item.getVariant() == null)
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            cartItems.add(new CartItem(product, null, quantity));
        }
    }

    // 🔥 Ajouter une variante
    public void addToCart(ProductVariant variant, int quantity) {
        Optional<CartItem> existingItem = cartItems.stream()
                .filter(item -> item.getVariant() != null
                        && item.getVariant().getId().equals(variant.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            cartItems.add(new CartItem(variant.getProduct(), variant, quantity));
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