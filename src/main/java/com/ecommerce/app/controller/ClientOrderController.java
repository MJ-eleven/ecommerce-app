package com.ecommerce.app.controller;

import com.ecommerce.app.model.Order;
import com.ecommerce.app.service.CurrencyService;
import com.ecommerce.app.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class ClientOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CurrencyService currencyService;

    @GetMapping
    public String listOrders(Model model) {
        // Récupérer l'utilisateur connecté
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Récupérer les commandes de l'utilisateur
        List<Order> orders = orderService.getOrdersByUser(username);

        System.out.println("🔍 Commandes trouvées pour " + username + " : " + orders.size());

        model.addAttribute("orders", orders);
        model.addAttribute("pageTitle", "Mes commandes - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/orders");
        return "order/list";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        model.addAttribute("pageTitle", "Commande #" + id + " - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/orders/" + id);
        return "order/detail";
    }
}
