package com.ecommerce.app.repository;

import com.ecommerce.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ============================================================
    // RECHERCHE
    // ============================================================
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // ============================================================
    // RÉCUPÉRATION DES COMMERÇANTS
    // ============================================================
    @Query("SELECT u FROM User u WHERE u.role = 'COMMERCANT'")
    List<User> findAllMerchants();

    @Query("SELECT u FROM User u WHERE u.role = 'COMMERCANT' AND u.blocked = true")
    List<User> findBlockedMerchants();

    @Query("SELECT u FROM User u WHERE u.role = 'COMMERCANT' AND u.blocked = false")
    List<User> findActiveMerchants();

    // ============================================================
    // COMPTEURS
    // ============================================================
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") String role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.blocked = :blocked")
    long countByRoleAndBlocked(@Param("role") String role, @Param("blocked") boolean blocked);

    // ============================================================
    // GESTION DES BLOCAGES EXPIRÉS
    // ============================================================
    @Query("SELECT u FROM User u WHERE u.role = 'COMMERCANT' AND u.blocked = true AND u.blockedUntil IS NOT NULL AND u.blockedUntil < :now")
    List<User> findMerchantsWithExpiredBlock(@Param("now") LocalDateTime now);

    // ============================================================
    // 📊 STATISTIQUES D'INSCRIPTION POUR L'ADMIN - Version PostgreSQL
    // ============================================================
    @Query(value = "SELECT TO_CHAR(u.created_at, 'YYYY-MM-DD') as date, COUNT(u) " +
            "FROM users u WHERE u.created_at >= :startDate " +
            "GROUP BY TO_CHAR(u.created_at, 'YYYY-MM-DD') ORDER BY date ASC",
            nativeQuery = true)
    List<Object[]> findRegistrationsLast7Days(@Param("startDate") LocalDateTime startDate);

    // Version sans paramètre (utilise 7 jours par défaut)
    default List<Object[]> findRegistrationsLast7Days() {
        return findRegistrationsLast7Days(LocalDateTime.now().minusDays(7));
    }

    // ============================================================
    // AUTRES STATISTIQUES
    // ============================================================
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'COMMERCANT' AND u.blocked = true")
    long countBlockedMerchants();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'COMMERCANT' AND u.blocked = false")
    long countActiveMerchants();
}