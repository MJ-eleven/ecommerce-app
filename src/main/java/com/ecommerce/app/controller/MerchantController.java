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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/merchant")
public class MerchantController {

    // ... autres imports et attributs ...

    private static final double XOF_TO_EUR = 1.0 / 655.96;  // 🔥 TAUX DE CONVERSION FCFA → EUR

    // ... autres méthodes ...

    // ============================================================
    // GESTION DES PRODUITS
    // ============================================================

    @PostMapping("/products/save")
    public String saveProduct(
            @ModelAttribute Product product,
            @RequestParam Long categoryId,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));

            // 🔥 CONVERTIR LE PRIX DE FCFA → EUR
            BigDecimal priceInXOF = product.getPrice();
            BigDecimal priceInEuro = priceInXOF.multiply(BigDecimal.valueOf(XOF_TO_EUR))
                    .setScale(2, RoundingMode.HALF_UP);
            product.setPrice(priceInEuro);

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

    @PostMapping("/variants/save")
    public String saveVariant(
            @RequestParam Long productId,
            @RequestParam String name,
            @RequestParam String attribute1,
            @RequestParam(required = false) String attribute2,
            @RequestParam BigDecimal price,  // Prix en FCFA
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

            // 🔥 CONVERTIR LE PRIX DE FCFA → EUR
            BigDecimal priceInEuro = price.multiply(BigDecimal.valueOf(XOF_TO_EUR))
                    .setScale(2, RoundingMode.HALF_UP);

            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setName(name);
            variant.setAttribute1(attribute1);
            variant.setAttribute2(attribute2);
            variant.setPrice(priceInEuro);  // Stocké en euros
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

    @PostMapping("/variants/update/{id}")
    public String updateVariant(
            @PathVariable Long id,
            @RequestParam Long productId,
            @RequestParam String name,
            @RequestParam String attribute1,
            @RequestParam(required = false) String attribute2,
            @RequestParam BigDecimal price,  // Prix en FCFA
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

            // 🔥 CONVERTIR LE PRIX DE FCFA → EUR
            BigDecimal priceInEuro = price.multiply(BigDecimal.valueOf(XOF_TO_EUR))
                    .setScale(2, RoundingMode.HALF_UP);

            variant.setName(name);
            variant.setAttribute1(attribute1);
            variant.setAttribute2(attribute2);
            variant.setPrice(priceInEuro);  // Stocké en euros
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

    // ... le reste du code ...
}