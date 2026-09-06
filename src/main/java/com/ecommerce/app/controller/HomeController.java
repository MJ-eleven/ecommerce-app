package com.ecommerce.app.controller;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.UserRepository;
import com.ecommerce.app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            Model model) {

        List<Product> products;

        if (keyword != null && !keyword.isEmpty() && categoryId != null) {
            products = productService.searchByKeywordAndCategory(keyword, categoryId);
            model.addAttribute("keyword", keyword);
            model.addAttribute("categoryId", categoryId);
        } else if (categoryId != null) {
            products = productService.getProductsByCategory(categoryId);
            model.addAttribute("categoryId", categoryId);
        } else if (keyword != null && !keyword.isEmpty()) {
            products = productService.searchProducts(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            products = productService.getFeaturedProducts();
        }

        // 🔥 TOP 5 PRODUITS LES PLUS VENDUS
        List<Product> topProducts = productService.getTop5BestSellers();

        // 🔥 Récupérer l'utilisateur connecté et ses favoris
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<Long> favoriteIds = new ArrayList<>();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String username = auth.getName();
            User currentUser = userRepository.findByUsername(username).orElse(null);
            if (currentUser != null) {
                favoriteIds = favoriteService.getFavoriteProductsByUser(currentUser).stream()
                        .map(Product::getId)
                        .collect(Collectors.toList());
            }
        }

        model.addAttribute("products", products);
        model.addAttribute("topProducts", topProducts);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("pageTitle", "Accueil - E-Shop");
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("cartService", cartService);
        model.addAttribute("currentUrl", "/");
        model.addAttribute("favoriteIds", favoriteIds);
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