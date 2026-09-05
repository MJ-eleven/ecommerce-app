package com.ecommerce.app.service;

import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DataLoaderService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        System.out.println("========================================");
        System.out.println("🚀 INITIALISATION DE L'APPLICATION");
        System.out.println("========================================");

        // ============================================================
        // CRÉER LE COMPTE ADMIN UNIQUEMENT
        // ============================================================

        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@ecommerce.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            userRepository.save(admin);

            System.out.println("✅ Compte ADMIN créé !");
            System.out.println("   👤 Username: admin");
            System.out.println("   🔑 Password: admin123");
        } else {
            System.out.println("ℹ️  Le compte admin existe déjà");
        }

        System.out.println("========================================");
        System.out.println("✅ APPLICATION PRÊTE");
        System.out.println("========================================");
        System.out.println("👤 Admin : admin / admin123");
        System.out.println("🌐 Admin : http://localhost:8080/admin/dashboard");
        System.out.println("🌐 Boutique : http://localhost:8080");
        System.out.println("========================================");
        System.out.println("📝 Les commerçants doivent créer :");
        System.out.println("   - Leurs catégories");
        System.out.println("   - Leurs produits");
        System.out.println("========================================");
    }
}