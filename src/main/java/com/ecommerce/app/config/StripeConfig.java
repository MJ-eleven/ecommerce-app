package com.ecommerce.app.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${stripe.secret.key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isEmpty()) {
            System.err.println("❌ STRIPE SECRET KEY MANQUANTE !");
            return;
        }
        Stripe.apiKey = secretKey;
        System.out.println("✅ Stripe configuré avec succès !");
        System.out.println("🔑 Clé secrète: " + secretKey.substring(0, 15) + "...");
    }
}