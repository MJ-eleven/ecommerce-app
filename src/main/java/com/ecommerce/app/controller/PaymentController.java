package com.ecommerce.app.controller;

import com.ecommerce.app.model.Order;
import com.ecommerce.app.service.OrderService;
import com.ecommerce.app.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private StripeService stripeService;

    @GetMapping("/checkout/{orderId}")
    public String paymentPage(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        model.addAttribute("order", order);
        model.addAttribute("publicKey", stripeService.getPublicKey());
        model.addAttribute("pageTitle", "Paiement - E-Shop");
        return "payment/checkout";
    }

    @PostMapping("/create-payment-intent")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody Map<String, Object> payload) {
        try {
            Long orderId = Long.valueOf(payload.get("orderId").toString());
            Order order = orderService.getOrderById(orderId);

            PaymentIntent paymentIntent = stripeService.createPaymentIntent(order);

            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret());
            response.put("paymentIntentId", paymentIntent.getId());

            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/webhook")
    @ResponseBody
    public String handleWebhook(@RequestBody String payload) {
        // Gérer les événements Stripe (paiement réussi, échoué, etc.)
        System.out.println("Webhook reçu: " + payload);
        return "OK";
    }

    @GetMapping("/success")
    public String paymentSuccess(@RequestParam String paymentIntentId, Model model) {
        // Récupérer la commande à partir du paymentIntentId
        // Mettre à jour le statut de la commande
        model.addAttribute("pageTitle", "Paiement réussi - E-Shop");
        return "payment/success";
    }

    @GetMapping("/cancel")
    public String paymentCancel() {
        return "payment/cancel";
    }
}