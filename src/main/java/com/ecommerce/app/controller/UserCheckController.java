package com.ecommerce.app.controller;

import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserCheckController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/check-user")
    public ResponseEntity<Map<String, Object>> checkUser(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                response.put("exists", false);
                return ResponseEntity.ok(response);
            }

            response.put("exists", true);
            response.put("blocked", !user.isEnabled());
            response.put("role", user.getRole());

            if (!user.isEnabled()) {
                response.put("message", "Votre compte a été bloqué par l'administrateur. Veuillez contacter le support.");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}