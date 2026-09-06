package com.ecommerce.app.service;

import com.ecommerce.app.model.Order;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    @Value("${stripe.public.key}")
    private String publicKey;

    public String getPublicKey() {
        // 🔥 FORCER LA CLÉ PUBLIQUE DIRECTEMENT
        String key = "pk_test_51Tx34aFM8n9T8nTEHthEYnakd4ZP8pyLP0oU1a4iV7keEA6mT1vBPb1IK1vwIPQlJdicDJWmElTJzIrTJCJY3E29004bhZi1S7";
        System.out.println("🔑 Clé publique Stripe: " + key);
        return key;
    }

    public PaymentIntent createPaymentIntent(Order order) throws StripeException {
        long amount = (long) (order.getTotalAmount() * 100);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency("eur")
                .setDescription("Commande #" + order.getId())
                .putMetadata("order_id", order.getId().toString())
                .putMetadata("customer_email", order.getCustomerEmail())
                .build();

        return PaymentIntent.create(params);
    }
}