package com.ecommerce.app.controller;

import com.ecommerce.app.model.Category;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.CategoryRepository;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.repository.UserRepository;
import com.ecommerce.app.service.CurrencyService;
import com.ecommerce.app.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private OrderService orderService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    // ============================================================
    // DASHBOARD AVEC STATISTIQUES ET GRAPHIQUES
    // ============================================================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User currentUser = getCurrentUser();

        // Statistiques globales
        long totalProducts = productRepository.count();
        long totalOrders = orderService.getAllOrders().size();
        long totalUsers = userRepository.count();
        long totalMerchants = userRepository.countByRole("COMMERCANT");
        long blockedMerchants = userRepository.countByRoleAndBlocked("COMMERCANT", true);
        long activeMerchants = totalMerchants - blockedMerchants;
        double totalRevenue = orderService.getTotalRevenue();

        // 🔥 Convertir le chiffre d'affaires dans la devise actuelle
        BigDecimal convertedRevenue = currencyService.convert(BigDecimal.valueOf(totalRevenue));

        // 1. Ventes des 7 derniers jours
        List<Object[]> salesData = orderService.getGlobalSalesLast7Days();
        List<String> salesLabels = new ArrayList<>();
        List<Double> salesValues = new ArrayList<>();
        for (Object[] row : salesData) {
            salesLabels.add(row[0].toString());
            salesValues.add((Double) row[1]);
        }
        while (salesLabels.size() < 7) {
            salesLabels.add(0, "J-" + (7 - salesLabels.size()));
            salesValues.add(0, 0.0);
        }

        // 2. Ventes par catégorie
        List<Object[]> categorySales = orderService.getGlobalSalesByCategory();
        List<String> categoryLabels = new ArrayList<>();
        List<Double> categoryValues = new ArrayList<>();
        for (Object[] row : categorySales) {
            categoryLabels.add(row[0].toString());
            categoryValues.add((Double) row[1]);
        }

        // 3. Commandes par statut
        List<Object[]> statusStats = orderService.getGlobalOrderStatusStats();
        List<String> statusLabels = new ArrayList<>();
        List<Long> statusValues = new ArrayList<>();
        for (Object[] row : statusStats) {
            statusLabels.add(row[0].toString());
            statusValues.add((Long) row[1]);
        }

        // 4. Top produits vendus
        List<Object[]> topProducts = orderService.getGlobalTopSellingProducts();
        List<String> topProductsLabels = new ArrayList<>();
        List<Long> topProductsValues = new ArrayList<>();
        for (Object[] row : topProducts) {
            topProductsLabels.add(row[0].toString());
            topProductsValues.add((Long) row[1]);
        }

        // 5. Évolution des inscriptions des 7 derniers jours
        List<Object[]> userRegistrations = userRepository.findRegistrationsLast7Days();
        List<String> registrationLabels = new ArrayList<>();
        List<Long> registrationValues = new ArrayList<>();
        for (Object[] row : userRegistrations) {
            registrationLabels.add(row[0].toString());
            registrationValues.add((Long) row[1]);
        }
        while (registrationLabels.size() < 7) {
            registrationLabels.add(0, "J-" + (7 - registrationLabels.size()));
            registrationValues.add(0, 0L);
        }

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalMerchants", totalMerchants);
        model.addAttribute("activeMerchants", activeMerchants);
        model.addAttribute("blockedMerchants", blockedMerchants);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("convertedRevenue", convertedRevenue);

        // Données pour les graphiques
        model.addAttribute("salesLabels", salesLabels);
        model.addAttribute("salesData", salesValues);
        model.addAttribute("categoryLabels", categoryLabels);
        model.addAttribute("categoryData", categoryValues);
        model.addAttribute("statusLabels", statusLabels);
        model.addAttribute("statusData", statusValues);
        model.addAttribute("topProductsLabels", topProductsLabels);
        model.addAttribute("topProductsData", topProductsValues);
        model.addAttribute("registrationLabels", registrationLabels);
        model.addAttribute("registrationData", registrationValues);

        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("pageTitle", "Dashboard - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/admin/dashboard");
        return "admin/dashboard";
    }

    // ============================================================
    // GESTION DES PRODUITS (CONSULTATION UNIQUEMENT)
    // ============================================================
    @GetMapping("/products")
    public String listProducts(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        model.addAttribute("pageTitle", "Gestion des produits - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/admin/products");
        return "admin/products";
    }

    // ============================================================
    // GESTION DES COMMERÇANTS
    // ============================================================
    @GetMapping("/merchants")
    public String listMerchants(Model model) {
        List<User> merchants = userRepository.findAllMerchants();
        model.addAttribute("merchants", merchants);
        model.addAttribute("pageTitle", "Gestion des commerçants - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/admin/merchants");
        return "admin/merchants";
    }

    @PostMapping("/merchants/unblock/{id}")
    public String unblockMerchant(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User merchant = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Commerçant non trouvé"));

            merchant.setBlocked(false);
            merchant.setBlockedUntil(null);
            merchant.setBlockedReason(null);
            merchant.setBlockedBy(null);
            merchant.setBlockedAt(null);

            userRepository.save(merchant);
            redirectAttributes.addFlashAttribute("success", "✅ Commerçant débloqué avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Erreur : " + e.getMessage());
        }
        return "redirect:/admin/merchants";
    }

    @PostMapping("/merchants/delete/{id}")
    public String deleteMerchant(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User merchant = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Commerçant non trouvé"));

            long productCount = productRepository.countByUser(merchant);
            if (productCount > 0) {
                redirectAttributes.addFlashAttribute("error", "❌ Impossible de supprimer ce commerçant car il a " + productCount + " produit(s)");
                return "redirect:/admin/merchants";
            }

            userRepository.delete(merchant);
            redirectAttributes.addFlashAttribute("success", "✅ Commerçant supprimé avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Erreur : " + e.getMessage());
        }
        return "redirect:/admin/merchants";
    }

    // ============================================================
    // GESTION DES COMMANDES (CONSULTATION UNIQUEMENT)
    // ============================================================
    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("pageTitle", "Commandes - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/admin/orders");
        return "admin/orders";
    }
}