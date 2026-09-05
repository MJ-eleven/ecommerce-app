package com.ecommerce.app.repository;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.active = true")
    List<Product> findActiveProducts();

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByNameContainingIgnoreCase(String keyword);

    // 🔥 RECHERCHE PAR MOT-CLÉ ET CATÉGORIE
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.category.id = :categoryId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> findByNameContainingAndCategoryId(@Param("keyword") String keyword, @Param("categoryId") Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.id DESC")
    List<Product> findLatestProducts();

    @Query("SELECT p FROM Product p WHERE p.user = :user AND p.active = true")
    List<Product> findByUser(@Param("user") User user);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.user = :user")
    long countByUser(@Param("user") User user);

    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.items i WHERE i.product.user = :user")
    long countOrdersByUser(@Param("user") User user);
}