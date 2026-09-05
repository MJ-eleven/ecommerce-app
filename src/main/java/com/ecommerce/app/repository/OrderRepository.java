package com.ecommerce.app.repository;

import com.ecommerce.app.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // ============================================================
    // RECHERCHE
    // ============================================================
    List<Order> findByCustomerEmail(String email);
    List<Order> findByStatus(String status);

    @Query("SELECT o FROM Order o WHERE o.merchant.id = :merchantId")
    List<Order> findByMerchantId(@Param("merchantId") Long merchantId);

    @Query("SELECT o FROM Order o WHERE o.user.username = :username")
    List<Order> findByUserUsername(@Param("username") String username);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId")
    List<Order> findByUserId(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o WHERE o.parentOrderId IS NULL")
    List<Order> findParentOrders();

    @Query("SELECT o FROM Order o WHERE o.parentOrderId = :parentOrderId")
    List<Order> findSubOrders(@Param("parentOrderId") Long parentOrderId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.merchant.id = :merchantId")
    long countByMerchantId(@Param("merchantId") Long merchantId);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.orderDate DESC")
    List<Order> findRecentOrdersByUserId(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // ============================================================
    // STATISTIQUES POUR LE COMMERCANT
    // ============================================================

    // 1. Ventes des 7 derniers jours (commerçant)
    @Query("SELECT FORMATDATETIME(o.orderDate, 'yyyy-MM-dd') as date, SUM(o.totalAmount) as total " +
            "FROM Order o WHERE o.merchant.id = :merchantId AND o.status != 'ANNULÉE' " +
            "AND o.orderDate >= :startDate GROUP BY FORMATDATETIME(o.orderDate, 'yyyy-MM-dd') ORDER BY date ASC")
    List<Object[]> findSalesLast7Days(@Param("merchantId") Long merchantId,
                                      @Param("startDate") LocalDateTime startDate);

    // 2. Ventes par catégorie (commerçant)
    @Query("SELECT c.name, SUM(o.totalAmount) FROM Order o " +
            "JOIN o.items i JOIN i.product p JOIN p.category c " +
            "WHERE o.merchant.id = :merchantId AND o.status != 'ANNULÉE' " +
            "GROUP BY c.name ORDER BY SUM(o.totalAmount) DESC")
    List<Object[]> findSalesByCategory(@Param("merchantId") Long merchantId);

    // 3. Commandes par statut (commerçant)
    @Query("SELECT o.status, COUNT(o) FROM Order o " +
            "WHERE o.merchant.id = :merchantId GROUP BY o.status")
    List<Object[]> findOrderStatusStats(@Param("merchantId") Long merchantId);

    // 4. Top produits vendus (commerçant)
    @Query("SELECT p.name, SUM(i.quantity) FROM Order o " +
            "JOIN o.items i JOIN i.product p " +
            "WHERE o.merchant.id = :merchantId AND o.status != 'ANNULÉE' " +
            "GROUP BY p.id ORDER BY SUM(i.quantity) DESC")
    List<Object[]> findTopSellingProducts(@Param("merchantId") Long merchantId);

    // 5. Revenus totaux (commerçant)
    @Query("SELECT SUM(o.totalAmount) FROM Order o " +
            "WHERE o.merchant.id = :merchantId AND o.status != 'ANNULÉE'")
    Double findTotalRevenueByMerchant(@Param("merchantId") Long merchantId);

    // 6. Commandes en attente (commerçant)
    @Query("SELECT COUNT(o) FROM Order o " +
            "WHERE o.merchant.id = :merchantId AND o.status = 'EN ATTENTE'")
    Long countPendingOrdersByMerchant(@Param("merchantId") Long merchantId);

    // ============================================================
    // 📊 STATISTIQUES GLOBALES POUR L'ADMIN
    // ============================================================

    // 1. Revenus totaux (global)
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status != 'ANNULÉE'")
    Double findTotalRevenue();

    // 2. Ventes des 7 derniers jours (global)
    @Query("SELECT FORMATDATETIME(o.orderDate, 'yyyy-MM-dd') as date, SUM(o.totalAmount) as total " +
            "FROM Order o WHERE o.status != 'ANNULÉE' AND o.orderDate >= :startDate " +
            "GROUP BY FORMATDATETIME(o.orderDate, 'yyyy-MM-dd') ORDER BY date ASC")
    List<Object[]> findGlobalSalesLast7Days(@Param("startDate") LocalDateTime startDate);

    // 3. Ventes par catégorie (global)
    @Query("SELECT c.name, SUM(o.totalAmount) FROM Order o " +
            "JOIN o.items i JOIN i.product p JOIN p.category c " +
            "WHERE o.status != 'ANNULÉE' GROUP BY c.name ORDER BY SUM(o.totalAmount) DESC")
    List<Object[]> findGlobalSalesByCategory();

    // 4. Commandes par statut (global)
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> findGlobalOrderStatusStats();

    // 5. Top produits vendus (global)
    @Query("SELECT p.name, SUM(i.quantity) FROM Order o " +
            "JOIN o.items i JOIN i.product p " +
            "WHERE o.status != 'ANNULÉE' GROUP BY p.id ORDER BY SUM(i.quantity) DESC")
    List<Object[]> findGlobalTopSellingProducts();
}