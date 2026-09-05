package com.ecommerce.app.controller;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.ProductVariant;
import com.ecommerce.app.service.CartService;
import com.ecommerce.app.service.CategoryService;
import com.ecommerce.app.service.CurrencyService;
import com.ecommerce.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CurrencyService currencyService;

    @GetMapping
    public String listProducts(
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
            products = productService.getAllProducts();
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("pageTitle", "Produits - E-Shop");
        model.addAttribute("cartService", cartService);
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/products");
        return "products/list";
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        List<ProductVariant> variants = productService.getVariantsByProduct(id);

        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("hasVariants", !variants.isEmpty());
        model.addAttribute("pageTitle", product.getName() + " - E-Shop");
        model.addAttribute("cartService", cartService);
        model.addAttribute("currencyService", currencyService);
        model.addAttribute("currentUrl", "/products/" + id);
        return "products/detail";
    }

    @PostMapping("/{id}/add-to-cart")
    public String addToCart(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(required = false) Long variantId,
            Model model) {

        try {
            if (variantId != null && variantId > 0) {
                // Ajouter une variante
                ProductVariant variant = productService.getVariantById(variantId);
                cartService.addToCart(variant, quantity);
            } else {
                // Ajouter un produit simple
                Product product = productService.getProductById(id);
                cartService.addToCart(product, quantity);
            }
            return "redirect:/products/" + id + "?added=true";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de l'ajout au panier : " + e.getMessage());
            return productDetail(id, model);
        }
    }
}