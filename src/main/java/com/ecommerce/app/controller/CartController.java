package com.ecommerce.app.controller;

import com.ecommerce.app.service.CartService;
import com.ecommerce.app.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CurrencyService currencyService;

    @GetMapping
    public String viewCart(Model model) {
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        model.addAttribute("totalItems", cartService.getTotalItems());
        model.addAttribute("pageTitle", "Panier - E-Shop");
        model.addAttribute("cartService", cartService);
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/cart");
        return "cart/view";
    }

    @PostMapping("/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId) {
        cartService.removeFromCart(productId);
        return "redirect:/cart";
    }

    @PostMapping("/update/{productId}")
    public String updateQuantity(@PathVariable Long productId, @RequestParam int quantity) {
        if (quantity <= 0) {
            cartService.removeFromCart(productId);
        } else {
            cartService.updateQuantity(productId, quantity);
        }
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart() {
        cartService.clearCart();
        return "redirect:/cart";
    }
}