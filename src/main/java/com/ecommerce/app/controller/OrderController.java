package com.ecommerce.app.controller;

import com.ecommerce.app.service.CartService;
import com.ecommerce.app.service.CurrencyService;
import com.ecommerce.app.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CurrencyService currencyService;

    @GetMapping("/checkout")
    public String checkoutForm(Model model) {
        if (cartService.getCartItems().isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("totalPrice", cartService.getTotalPrice());
        model.addAttribute("pageTitle", "Validation - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/order/checkout");
        return "order/checkout";
    }

    @PostMapping("/create")
    public String createOrder(
            @RequestParam String customerName,
            @RequestParam String customerEmail,
            @RequestParam String customerPhone,
            @RequestParam String shippingAddress,
            Model model) {

        if (cartService.getCartItems().isEmpty()) {
            return "redirect:/cart";
        }

        try {
            // 🔥 Créer plusieurs commandes (une par vendeur)
            List<com.ecommerce.app.model.Order> orders = orderService.createOrders(
                    customerName,
                    customerEmail,
                    customerPhone,
                    shippingAddress
            );

            model.addAttribute("orders", orders);
            model.addAttribute("totalOrders", orders.size());
            model.addAttribute("pageTitle", "Confirmation - E-Shop");
            model.addAttribute("currencyService", currencyService);
            model.addAttribute("currentUrl", "/order/confirmation");
            return "order/confirmation";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la création de la commande : " + e.getMessage());
            return "order/checkout";
        }
    }
}