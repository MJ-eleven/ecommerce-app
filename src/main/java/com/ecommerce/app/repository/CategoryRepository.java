package com.ecommerce.app.repository;

import com.ecommerce.app.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;    // ← IMPORT MANQUANT !
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // ============================================================
    // Recherche par nom
    // ============================================================

    Optional<Category> findByName(String name);

    // ============================================================
    // Vérification d'existence
    // ============================================================

    boolean existsByName(String name);

    // ============================================================
    // Compteurs pour le dashboard
    // ============================================================

    @Query("SELECT COUNT(c) FROM Category c")
    long countCategories();

    // ============================================================
    // Catégories avec le nombre de produits
    // ============================================================

    @Query("SELECT c, COUNT(p) FROM Category c LEFT JOIN c.products p WHERE p.active = true GROUP BY c")
    List<Object[]> findCategoriesWithProductCount();
}