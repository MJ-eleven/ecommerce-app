package com.ecommerce.app.controller;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.service.CurrencyService;
import com.ecommerce.app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CurrencyService currencyService;

    @GetMapping("/")
    public String home(Model model) {
        List<Product> featuredProducts = productService.getFeaturedProducts();
        model.addAttribute("products", featuredProducts);
        model.addAttribute("pageTitle", "Accueil - E-Shop");
        model.addAttribute("currencyService", currencyService);
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