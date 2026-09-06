package com.ecommerce.app.controller;

import com.ecommerce.app.model.*;
import com.ecommerce.app.repository.*;
import com.ecommerce.app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/merchant")
public class MerchantController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private NotificationService notificationService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    // ============================================================
    // DASHBOARD
    // ============================================================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User currentUser = getCurrentUser();

        long totalProducts = productRepository.countByUser(currentUser);
        long totalOrders = orderService.countOrdersByMerchant(currentUser.getId());
        double totalRevenue = orderService.getTotalRevenueByMerchant(currentUser.getId());
        long pendingOrders = orderService.countPendingOrdersByMerchant(currentUser.getId());

        BigDecimal convertedRevenue = currencyService.convertFromXOF(BigDecimal.valueOf(totalRevenue));

        List<Object[]> salesData = orderService.getSalesLast7Days(currentUser.getId());
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

        List<Object[]> categorySales = orderService.getSalesByCategory(currentUser.getId());
        List<String> categoryLabels = new ArrayList<>();
        List<Double> categoryValues = new ArrayList<>();
        for (Object[] row : categorySales) {
            categoryLabels.add(row[0].toString());
            categoryValues.add((Double) row[1]);
        }

        List<Object[]> statusStats = orderService.getOrderStatusStats(currentUser.getId());
        List<String> statusLabels = new ArrayList<>();
        List<Long> statusValues = new ArrayList<>();
        for (Object[] row : statusStats) {
            statusLabels.add(row[0].toString());
            statusValues.add((Long) row[1]);
        }

        List<Object[]> topProducts = orderService.getTopSellingProducts(currentUser.getId());
        List<String> topProductsLabels = new ArrayList<>();
        List<Long> topProductsValues = new ArrayList<>();
        for (Object[] row : topProducts) {
            topProductsLabels.add(row[0].toString());
            topProductsValues.add((Long) row[1]);
        }

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("convertedRevenue", convertedRevenue);
        model.addAttribute("pendingOrders", pendingOrders);

        model.addAttribute("salesLabels", salesLabels);
        model.addAttribute("salesData", salesValues);
        model.addAttribute("categoryLabels", categoryLabels);
        model.addAttribute("categoryData", categoryValues);
        model.addAttribute("statusLabels", statusLabels);
        model.addAttribute("statusData", statusValues);
        model.addAttribute("topProductsLabels", topProductsLabels);
        model.addAttribute("topProductsData", topProductsValues);

        model.addAttribute("products", productRepository.findByUser(currentUser));
        model.addAttribute("pageTitle", "Dashboard - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/merchant/dashboard");
        return "merchant/dashboard";
    }

    // ============================================================
    // GESTION DES PRODUITS
    // ============================================================
    @GetMapping("/products")
    public String listProducts(Model model) {
        User currentUser = getCurrentUser();
        List<Product> products = productRepository.findByUser(currentUser);
        for (Product product : products) {
            product.getVariants().size();
        }
        model.addAttribute("products", products);
        model.addAttribute("pageTitle", "Mes produits - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/merchant/products");
        return "merchant/products";
    }

    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("pageTitle", "Ajouter un produit - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/merchant/products/add");
        return "merchant/product-form";
    }

    // 🔥 SANS CONVERSION : le prix est directement en FCFA
    @PostMapping("/products/save")
    public String saveProduct(
            @ModelAttribute Product product,
            @RequestParam Long categoryId,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));

            product.setCategory(category);
            product.setUser(currentUser);
            product.setActive(true);

            productRepository.save(product);
            redirectAttributes.addFlashAttribute("success", "✅ Produit ajouté avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Erreur : " + e.getMessage());
        }
        return "redirect:/merchant/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

            if (!product.getUser().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "❌ Vous n'êtes pas le propriétaire de ce produit");
                return "redirect:/merchant/products";
            }

            model.addAttribute("product", product);
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("pageTitle", "Modifier le produit - E-Shop");
            model.addAttribute("currencyService", currencyService);
            model.addAttribute("currentUrl", "/merchant/products/edit/" + id);
            return "merchant/product-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Erreur : " + e.getMessage());
            return "redirect:/merchant/products";
        }
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

            if (product.getUser().getId().equals(currentUser.getId())) {
                if (product.hasVariants()) {
                    productVariantRepository.deleteAll(product.getVariants());
                }
                productRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "✅ Produit supprimé avec succès !");
            } else {
                redirectAttributes.addFlashAttribute("error", "❌ Vous n'avez pas le droit de supprimer ce produit");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Erreur : " + e.getMessage());
        }
        return "redirect:/merchant/products";
    }

    // ============================================================
    // GESTION DES VARIANTES
    // ============================================================
    @GetMapping("/variants/product/{productId}")
    public String listVariants(@PathVariable Long productId, Model model) {
        User currentUser = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        if (!product.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/merchant/products?error=Unauthorized";
        }

        List<ProductVariant> variants = productVariantRepository.findByProductId(productId);
        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("pageTitle", "Variantes - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/merchant/variants/product/" + productId);
        return "merchant/variants";
    }

    @GetMapping("/variants/add/{productId}")
    public String addVariantForm(@PathVariable Long productId, Model model) {
        User currentUser = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        if (!product.getUser().getId().equals(currentUser.getId())) {
            return "redirect:/merchant/products?error=Unauthorized";
        }

        model.addAttribute("product", product);
        model.addAttribute("variant", new ProductVariant());
        model.addAttribute("pageTitle", "Ajouter une variante - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/merchant/variants/add/" + productId);
        return "merchant/variant-form";
    }

    // 🔥 SANS CONVERSION : le prix est directement en FCFA
    @PostMapping("/variants/save")
    public String saveVariant(
            @RequestParam Long productId,
            @RequestParam String name,
            @RequestParam String attribute1,
            @RequestParam(required = false) String attribute2,
            @RequestParam BigDecimal price,
            @RequestParam int stockQuantity,
            @RequestParam String sku,
            @RequestParam(required = false) String imageUrl,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = getCurrentUser();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

            if (!product.getUser().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "❌ Vous n'êtes pas le propriétaire de ce produit");
                return "redirect:/merchant/products";
            }

            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setName(name);
            variant.setAttribute1(attribute1);
            variant.setAttribute2(attribute2);
            variant.setPrice(price);  // Directement en FCFA
            variant.setStockQuantity(stockQuantity);
            variant.setSku(sku);
            variant.setImageUrl(imageUrl);
            variant.setActive(true);

            productVariantRepository.save(variant);
            redirectAttributes.addFlashAttribute("success", "✅ Variante ajoutée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Erreur : " + e.getMessage());
        }
        return "redirect:/merchant/variants/product/" + productId;
    }

    @GetMapping("/variants/edit/{id}")
    public String editVariantForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ProductVariant variant = productVariantRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Variante non trouvée"));

            User currentUser = getCurrentUser();
            Product product = variant.getProduct();

            if (!product.getUser().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "❌ Vous n'êtes pas le propriétaire de ce produit");
                return "redirect:/merchant/products";
            }

            model.addAttribute("product", product);
            model.addAttribute("variant", variant);
            model.addAttribute("pageTitle", "Modifier la variante - E-Shop");
            model.addAttribute("currencyService", currencyService);
            model.addAttribute("currentUrl", "/merchant/variants/edit/" + id);
            return "merchant/variant-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Erreur : " + e.getMessage());
            return "redirect:/merchant/variants/product/" + id;
        }
    }

    // 🔥 SANS CONVERSION : le prix est directement en FCFA
    @PostMapping("/variants/update/{id}")
    public String updateVariant(
            @PathVariable Long id,
            @RequestParam Long productId,
            @RequestParam String name,
            @RequestParam String attribute1,
            @RequestParam(required = false) String attribute2,
            @RequestParam BigDecimal price,
            @RequestParam int stockQuantity,
            @RequestParam String sku,
            @RequestParam(required = false) String imageUrl,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = getCurrentUser();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

            if (!product.getUser().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "❌ Vous n'êtes pas le propriétaire de ce produit");
                return "redirect:/merchant/products";
            }

            ProductVariant variant = productVariantRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Variante non trouvée"));

            variant.setName(name);
            variant.setAttribute1(attribute1);
            variant.setAttribute2(attribute2);
            variant.setPrice(price);  // Directement en FCFA
            variant.setStockQuantity(stockQuantity);
            variant.setSku(sku);
            variant.setImageUrl(imageUrl);

            productVariantRepository.save(variant);
            redirectAttributes.addFlashAttribute("success", "✅ Variante modifiée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Erreur : " + e.getMessage());
        }
        return "redirect:/merchant/variants/product/" + productId;
    }

    @GetMapping("/variants/delete/{id}")
    public String deleteVariant(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ProductVariant variant = productVariantRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Variante non trouvée"));

            User currentUser = getCurrentUser();
            Product product = variant.getProduct();

            if (!product.getUser().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "❌ Vous n'êtes pas le propriétaire de ce produit");
                return "redirect:/merchant/products";
            }

            productVariantRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "✅ Variante supprimée avec succès !");
            return "redirect:/merchant/variants/product/" + product.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Erreur : " + e.getMessage());
            return "redirect:/merchant/products";
        }
    }

    // ============================================================
    // GESTION DES CATÉGORIES
    // ============================================================
    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("pageTitle", "Mes catégories - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/merchant/categories");
        return "merchant/categories";
    }

    @GetMapping("/categories/add")
    public String addCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("pageTitle", "Ajouter une catégorie - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/merchant/categories/add");
        return "merchant/category-form";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        try {
            if (categoryRepository.existsByName(category.getName())) {
                redirectAttributes.addFlashAttribute("error", "❌ Une catégorie avec ce nom existe déjà");
                return "redirect:/merchant/categories";
            }
            categoryService.initializeDefaultAttributes(category);
            categoryRepository.save(category);
            redirectAttributes.addFlashAttribute("success", "✅ Catégorie ajoutée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Erreur : " + e.getMessage());
        }
        return "redirect:/merchant/categories";
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategoryForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));
            model.addAttribute("category", category);
            model.addAttribute("pageTitle", "Modifier la catégorie - E-Shop");
            model.addAttribute("currencyService", currencyService);
            model.addAttribute("currentUrl", "/merchant/categories/edit/" + id);
            return "merchant/category-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Erreur : " + e.getMessage());
            return "redirect:/merchant/categories";
        }
    }

    @PostMapping("/categories/update/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @ModelAttribute Category category,
                                 RedirectAttributes redirectAttributes) {
        try {
            Category existingCategory = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));

            Category categoryWithSameName = categoryRepository.findByName(category.getName()).orElse(null);
            if (categoryWithSameName != null && !categoryWithSameName.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "❌ Une catégorie avec ce nom existe déjà");
                return "redirect:/merchant/categories";
            }

            existingCategory.setName(category.getName());
            existingCategory.setDescription(category.getDescription());
            existingCategory.setAttributeNames(category.getAttributeNames());
            existingCategory.setAttributeValues(category.getAttributeValues());
            categoryRepository.save(existingCategory);
            redirectAttributes.addFlashAttribute("success", "✅ Catégorie modifiée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Erreur : " + e.getMessage());
        }
        return "redirect:/merchant/categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));

            if (!category.getProducts().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "❌ Impossible de supprimer cette catégorie car elle contient des produits");
                return "redirect:/merchant/categories";
            }

            categoryRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "✅ Catégorie supprimée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Erreur : " + e.getMessage());
        }
        return "redirect:/merchant/categories";
    }

    // ============================================================
    // GESTION DES COMMANDES
    // ============================================================
    @GetMapping("/orders")
    public String listOrders(Model model) {
        User currentUser = getCurrentUser();
        List<Order> orders = orderService.getOrdersByMerchant(currentUser.getId());
        model.addAttribute("orders", orders);
        model.addAttribute("pageTitle", "Mes commandes - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/merchant/orders");
        return "merchant/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        User currentUser = getCurrentUser();
        Order order = orderService.getOrderById(id);

        if (!order.getMerchant().getId().equals(currentUser.getId())) {
            return "redirect:/merchant/orders?error=Unauthorized";
        }

        model.addAttribute("order", order);
        model.addAttribute("pageTitle", "Commande #" + id + " - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/merchant/orders/" + id);
        return "merchant/order-detail";
    }

    @PostMapping("/orders/update-status/{id}")
    public String updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {

        try {
            User currentUser = getCurrentUser();
            Order order = orderService.getOrderById(id);

            if (!order.getMerchant().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "❌ Vous n'êtes pas autorisé à modifier cette commande");
                return "redirect:/merchant/orders";
            }

            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "✅ Statut de la commande mis à jour : " + status);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Erreur : " + e.getMessage());
        }
        return "redirect:/merchant/orders";
    }
}