package com.ecommerce.app.controller;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.Review;
import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.UserRepository;
import com.ecommerce.app.service.ProductService;
import com.ecommerce.app.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

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
    public String addReview(@PathVariable Long productId,
                            @RequestParam int rating,
                            @RequestParam(required = false) String comment,
                            RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser();
            Product product = productService.getProductById(productId);

            reviewService.addReview(product, user, rating, comment);

            redirectAttributes.addFlashAttribute("success", "✅ Votre avis a été ajouté !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Erreur : " + e.getMessage());
        }
        return "redirect:/products/" + productId;
    }

    @GetMapping("/product/{productId}")
    @ResponseBody
    public List<Review> getProductReviews(@PathVariable Long productId) {
        Product product = productService.getProductById(productId);
        return reviewService.getReviewsByProduct(product);
    }
}