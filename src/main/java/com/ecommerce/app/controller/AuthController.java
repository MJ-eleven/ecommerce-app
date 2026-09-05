package com.ecommerce.app.controller;

import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 🔥 CONSTRUCTEUR MANUEL (au lieu de @RequiredArgsConstructor)
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null && error.equals("blocked")) {
            model.addAttribute("blocked", true);
        } else if (error != null) {
            model.addAttribute("error", true);
        }

        if (logout != null) {
            model.addAttribute("logout", true);
        }

        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", List.of("USER", "COMMERCANT"));
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam(defaultValue = "USER") String role,
            Model model) {

        // Vérifier si les mots de passe correspondent
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Les mots de passe ne correspondent pas");
            model.addAttribute("roles", List.of("USER", "COMMERCANT"));
            return "auth/register";
        }

        // Vérifier si l'utilisateur existe déjà
        if (userRepository.existsByUsername(username)) {
            model.addAttribute("error", "Ce nom d'utilisateur est déjà pris");
            model.addAttribute("roles", List.of("USER", "COMMERCANT"));
            return "auth/register";
        }

        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "Cet email est déjà utilisé");
            model.addAttribute("roles", List.of("USER", "COMMERCANT"));
            return "auth/register";
        }

        // Créer l'utilisateur avec le rôle choisi
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);

        userRepository.save(user);

        // Message de succès
        String roleMessage = role.equals("COMMERCANT") ? "commerçant" : "client";
        model.addAttribute("success", "✅ Inscription réussie ! Votre compte " + roleMessage + " a été créé avec succès.");

        // Rediriger vers la page de connexion avec message de succès
        return "redirect:/login?registered=true";
    }
}