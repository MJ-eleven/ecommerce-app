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
        System.out.println("🔑 Clé publique Stripe: " + publicKey);
        return publicKey;
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