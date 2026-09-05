package com.ecommerce.app.controller;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.service.CartService;
import com.ecommerce.app.service.CategoryService;
import com.ecommerce.app.service.CurrencyService;
import com.ecommerce.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private CartService cartService;

    @GetMapping("/")
    public String home(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            Model model) {

        List<Product> products;

        // 🔥 RECHERCHE PAR MOT-CLÉ ET CATÉGORIE
        if (keyword != null && !keyword.isEmpty() && categoryId != null) {
            // Recherche par mot-clé ET catégorie
            products = productService.searchByKeywordAndCategory(keyword, categoryId);
            model.addAttribute("keyword", keyword);
            model.addAttribute("categoryId", categoryId);
        } else if (categoryId != null) {
            // Recherche par catégorie uniquement
            products = productService.getProductsByCategory(categoryId);
            model.addAttribute("categoryId", categoryId);
        } else if (keyword != null && !keyword.isEmpty()) {
            // Recherche par mot-clé uniquement
            products = productService.searchProducts(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            // Tous les produits (en vedette)
            products = productService.getFeaturedProducts();
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("pageTitle", "Accueil - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("cartService", cartService);
        model.addAttribute("currentUrl", "/");
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "À propos - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/about");
        return "about";
    }
}