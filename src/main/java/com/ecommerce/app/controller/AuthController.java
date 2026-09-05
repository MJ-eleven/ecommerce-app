package com.ecommerce.app.controller;

import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // 🔥 API DE VÉRIFICATION DU BLOCAGE
    @GetMapping("/api/check-blocked")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkBlocked(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                response.put("exists", false);
                return ResponseEntity.ok(response);
            }

            response.put("exists", true);
            response.put("blocked", !user.isEnabled());
            response.put("username", user.getUsername());

            if (!user.isEnabled()) {
                response.put("message", "Votre compte a été bloqué par l'administrateur.");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
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