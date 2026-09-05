package com.ecommerce.app.config;

import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class BlockedUserFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Vérifier si c'est une tentative de connexion
        if (request.getRequestURI().equals("/login") && request.getMethod().equals("POST")) {
            String username = request.getParameter("username");

            if (username != null && !username.isEmpty()) {
                try {
                    User user = userRepository.findByUsername(username).orElse(null);

                    if (user != null && !user.isEnabled()) {
                        System.out.println("❌ " + username + " est BLOQUÉ - Redirection vers login?error=blocked");
                        response.sendRedirect("/login?error=blocked");
                        return;
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Erreur lors de la vérification du blocage: " + e.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}