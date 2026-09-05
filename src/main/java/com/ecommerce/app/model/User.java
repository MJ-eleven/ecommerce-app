package com.ecommerce.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String role = "USER";

    @Column(nullable = false)
    private boolean enabled = true;

    // 🔥 CHAMPS DE BLOCAGE
    @Column(name = "is_blocked")
    private boolean blocked = false;

    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;

    @Column(name = "blocked_reason")
    private String blockedReason;

    @Column(name = "blocked_by")
    private String blockedBy;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ============================================================
    // GETTERS ET SETTERS
    // ============================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public LocalDateTime getBlockedUntil() { return blockedUntil; }
    public void setBlockedUntil(LocalDateTime blockedUntil) { this.blockedUntil = blockedUntil; }

    public String getBlockedReason() { return blockedReason; }
    public void setBlockedReason(String blockedReason) { this.blockedReason = blockedReason; }

    public String getBlockedBy() { return blockedBy; }
    public void setBlockedBy(String blockedBy) { this.blockedBy = blockedBy; }

    public LocalDateTime getBlockedAt() { return blockedAt; }
    public void setBlockedAt(LocalDateTime blockedAt) { this.blockedAt = blockedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ============================================================
    // MÉTHODES UTILITAIRES
    // ============================================================

    public boolean isCurrentlyBlocked() {
        if (!blocked) return false;
        if (blockedUntil == null) return true; // Blocage définitif
        return LocalDateTime.now().isBefore(blockedUntil); // Blocage temporaire
    }

    public String getBlockStatus() {
        if (!blocked) return "ACTIF";
        if (blockedUntil == null) return "BLOQUÉ DÉFINITIVEMENT";
        if (LocalDateTime.now().isBefore(blockedUntil)) {
            return "BLOQUÉ JUSQU'AU " + blockedUntil.toString();
        }
        return "DÉBLOQUÉ";
    }
}