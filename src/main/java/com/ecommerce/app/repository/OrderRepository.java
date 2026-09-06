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

    @Query(value = "SELECT TO_CHAR(o.order_date, 'YYYY-MM-DD') as date, SUM(o.total_amount) as total " +
            "FROM orders o WHERE o.merchant_id = :merchantId AND o.status != 'ANNULÉE' " +
            "AND o.order_date >= :startDate GROUP BY TO_CHAR(o.order_date, 'YYYY-MM-DD') ORDER BY date ASC",
            nativeQuery = true)
    List<Object[]> findSalesLast7Days(@Param("merchantId") Long merchantId,
                                      @Param("startDate") LocalDateTime startDate);

    @Query("SELECT c.name, SUM(o.totalAmount) FROM Order o " +
            "JOIN o.items i JOIN i.product p JOIN p.category c " +
            "WHERE o.merchant.id = :merchantId AND o.status != 'ANNULÉE' " +
            "GROUP BY c.name ORDER BY SUM(o.totalAmount) DESC")
    List<Object[]> findSalesByCategory(@Param("merchantId") Long merchantId);

    @Query("SELECT o.status, COUNT(o) FROM Order o " +
            "WHERE o.merchant.id = :merchantId GROUP BY o.status")
    List<Object[]> findOrderStatusStats(@Param("merchantId") Long merchantId);

    @Query("SELECT p.name, SUM(i.quantity) FROM Order o " +
            "JOIN o.items i JOIN i.product p " +
            "WHERE o.merchant.id = :merchantId AND o.status != 'ANNULÉE' " +
            "GROUP BY p.id ORDER BY SUM(i.quantity) DESC")
    List<Object[]> findTopSellingProducts(@Param("merchantId") Long merchantId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o " +
            "WHERE o.merchant.id = :merchantId AND o.status != 'ANNULÉE'")
    Double findTotalRevenueByMerchant(@Param("merchantId") Long merchantId);

    @Query("SELECT COUNT(o) FROM Order o " +
            "WHERE o.merchant.id = :merchantId AND o.status = 'EN ATTENTE'")
    Long countPendingOrdersByMerchant(@Param("merchantId") Long merchantId);

    // ============================================================
    // STATISTIQUES GLOBALES POUR L'ADMIN
    // ============================================================

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status != 'ANNULÉE'")
    Double findTotalRevenue();

    @Query(value = "SELECT TO_CHAR(o.order_date, 'YYYY-MM-DD') as date, SUM(o.total_amount) as total " +
            "FROM orders o WHERE o.status != 'ANNULÉE' AND o.order_date >= :startDate " +
            "GROUP BY TO_CHAR(o.order_date, 'YYYY-MM-DD') ORDER BY date ASC",
            nativeQuery = true)
    List<Object[]> findGlobalSalesLast7Days(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT c.name, SUM(o.totalAmount) FROM Order o " +
            "JOIN o.items i JOIN i.product p JOIN p.category c " +
            "WHERE o.status != 'ANNULÉE' GROUP BY c.name ORDER BY SUM(o.totalAmount) DESC")
    List<Object[]> findGlobalSalesByCategory();

    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> findGlobalOrderStatusStats();

    @Query("SELECT p.name, SUM(i.quantity) FROM Order o " +
            "JOIN o.items i JOIN i.product p " +
            "WHERE o.status != 'ANNULÉE' GROUP BY p.id ORDER BY SUM(i.quantity) DESC")
    List<Object[]> findGlobalTopSellingProducts();

    // ============================================================
    // TOP 5 PRODUITS LES PLUS VENDUS
    // ============================================================
    @Query("SELECT p.id, p.name, p.imageUrl, p.price, SUM(i.quantity) as totalVendu " +
            "FROM Order o " +
            "JOIN o.items i " +
            "JOIN i.product p " +
            "WHERE o.status != 'ANNULÉE' " +
            "GROUP BY p.id, p.name, p.imageUrl, p.price " +
            "ORDER BY totalVendu DESC")
    List<Object[]> findTop5BestSellers();

    // ============================================================
    // 🔥 COMMANDES AVEC STATUT NULL
    // ============================================================
    @Query("SELECT o FROM Order o WHERE o.status IS NULL OR o.status = ''")
    List<Order> findOrdersWithNullStatus();
}