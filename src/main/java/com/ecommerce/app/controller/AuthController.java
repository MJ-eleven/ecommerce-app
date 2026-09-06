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

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "username", required = false) String username,
            Model model) {

        System.out.println("🔍 PAGE LOGIN - error: " + error + ", username: " + username);

        // 🔥 VÉRIFIER LE BLOCAGE
        if (username != null && !username.isEmpty()) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null && !user.isEnabled()) {
                model.addAttribute("blocked", true);
                model.addAttribute("errorMessage", "Votre compte a été bloqué par l'administrateur.");
                System.out.println("🔴 " + username + " est BLOQUÉ !");
            }
        }

        if (error != null && !"blocked".equals(error)) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", "Nom d'utilisateur ou mot de passe incorrect.");
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

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Les mots de passe ne correspondent pas");
            model.addAttribute("roles", List.of("USER", "COMMERCANT"));
            return "auth/register";
        }

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

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);

        userRepository.save(user);

        return "redirect:/login?registered=true";
    }
}