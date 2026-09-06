package com.ecommerce.app.controller;

import com.ecommerce.app.model.Favorite;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.UserRepository;
import com.ecommerce.app.service.FavoriteService;
import com.ecommerce.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    @PostMapping("/add/{productId}")
    public String addFavorite(@PathVariable Long productId,
                              @RequestParam(required = false) String redirect,
                              RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser();
            Product product = productService.getProductById(productId);
            favoriteService.addFavorite(user, product);
            redirectAttributes.addFlashAttribute("success", "❤️ Ajouté aux favoris !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ " + e.getMessage());
        }
        return "redirect:" + (redirect != null ? redirect : "/products/" + productId);
    }

    @PostMapping("/remove/{productId}")
    public String removeFavorite(@PathVariable Long productId,
                                 @RequestParam(required = false) String redirect,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser();
            Product product = productService.getProductById(productId);
            favoriteService.removeFavorite(user, product);
            redirectAttributes.addFlashAttribute("success", "💔 Retiré des favoris");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ " + e.getMessage());
        }
        return "redirect:" + (redirect != null ? redirect : "/products/" + productId);
    }

    @GetMapping
    public String listFavorites(Model model) {
        User user = getCurrentUser();
        List<Product> favorites = favoriteService.getFavoriteProductsByUser(user);
        model.addAttribute("favorites", favorites);
        model.addAttribute("pageTitle", "Mes favoris - E-Shop");
        return "favorites/list";
    }
}