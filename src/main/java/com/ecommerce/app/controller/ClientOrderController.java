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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/orders")
public class ClientOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CurrencyService currencyService;

    @GetMapping
    public String listOrders(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Récupérer les commandes parentes (celles du client)
        List<Order> parentOrders = orderService.getParentOrders();

        // Filtrer par utilisateur
        List<Order> userOrders = parentOrders.stream()
                .filter(o -> o.getUser() != null && o.getUser().getUsername().equals(username))
                .collect(Collectors.toList());

        System.out.println("🔍 Commandes trouvées pour " + username + " : " + userOrders.size());

        model.addAttribute("orders", userOrders);
        model.addAttribute("pageTitle", "Mes commandes - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/orders");
        return "order/list";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);

        // Si c'est une commande parente, récupérer les sous-commandes
        List<Order> subOrders = orderService.getSubOrders(id);

        // Récupérer toutes les commandes liées pour le statut global
        // Si c'est une sous-commande, trouver le parent
        Order displayOrder = order;
        List<Order> displaySubOrders = subOrders;

        if (order.getParentOrderId() != null) {
            // C'est une sous-commande, on affiche le parent
            displayOrder = orderService.getOrderById(order.getParentOrderId());
            displaySubOrders = orderService.getSubOrders(order.getParentOrderId());
        }

        model.addAttribute("order", displayOrder);
        model.addAttribute("subOrders", displaySubOrders);
        model.addAttribute("pageTitle", "Commande #" + displayOrder.getId() + " - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/orders/" + id);
        return "order/detail";
    }
}