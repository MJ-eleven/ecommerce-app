package com.ecommerce.app.service;

import com.ecommerce.app.model.CartItem;
import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.OrderItem;
import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.OrderRepository;
import com.ecommerce.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    // ============================================================
    // CRÉATION DE COMMANDE
    // ============================================================
    @Transactional
    public List<Order> createOrders(String customerName, String customerEmail,
                                    String customerPhone, String shippingAddress) {

        List<CartItem> cartItems = cartService.getCartItems();

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Le panier est vide");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);

        Map<Long, List<CartItem>> itemsByMerchant = new HashMap<>();

        for (CartItem item : cartItems) {
            Long merchantId = item.getProduct().getUser().getId();
            itemsByMerchant.computeIfAbsent(merchantId, k -> new ArrayList<>()).add(item);
        }

        List<Order> createdOrders = new ArrayList<>();
        Order parentOrder = null;

        for (Map.Entry<Long, List<CartItem>> entry : itemsByMerchant.entrySet()) {
            Long merchantId = entry.getKey();
            List<CartItem> merchantItems = entry.getValue();

            User merchant = userRepository.findById(merchantId)
                    .orElseThrow(() -> new RuntimeException("Vendeur non trouvé"));

            Order order = new Order();
            order.setCustomerName(customerName);
            order.setCustomerEmail(customerEmail);
            order.setCustomerPhone(customerPhone);
            order.setShippingAddress(shippingAddress);
            order.setOrderDate(LocalDateTime.now());
            order.setStatus("EN ATTENTE");
            order.setMerchant(merchant);
            order.setUser(currentUser);

            double total = 0;
            for (CartItem item : merchantItems) {
                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(item.getProduct());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setPrice(item.getProduct().getPrice().doubleValue());
                order.getItems().add(orderItem);
                total += item.getTotalPrice();
            }
            order.setTotalAmount(total);

            Order savedOrder = orderRepository.save(order);
            createdOrders.add(savedOrder);

            if (parentOrder == null) {
                parentOrder = savedOrder;
            } else {
                savedOrder.setParentOrderId(parentOrder.getId());
                orderRepository.save(savedOrder);
            }
        }

        cartService.clearCart();
        return createdOrders;
    }

    public Order createOrder(String customerName, String customerEmail,
                             String customerPhone, String shippingAddress) {
        List<Order> orders = createOrders(customerName, customerEmail, customerPhone, shippingAddress);
        return orders.isEmpty() ? null : orders.get(0);
    }

    // ============================================================
    // RÉCUPÉRATION DES COMMANDES
    // ============================================================
    public List<Order> getOrdersByMerchant(Long merchantId) {
        return orderRepository.findByMerchantId(merchantId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
    }

    public List<Order> getOrdersByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmail(email);
    }

    public List<Order> getOrdersByUser(String username) {
        return orderRepository.findByUserUsername(username);
    }

    public List<Order> getParentOrders() {
        return orderRepository.findParentOrders();
    }

    public List<Order> getSubOrders(Long parentOrderId) {
        return orderRepository.findSubOrders(parentOrderId);
    }

    // ============================================================
    // MISE À JOUR
    // ============================================================
    public Order updateOrderStatus(Long id, String status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    // ============================================================
    // 📊 STATISTIQUES POUR LE COMMERCANT
    // ============================================================

    public long countOrdersByMerchant(Long merchantId) {
        return orderRepository.countByMerchantId(merchantId);
    }

    public double getTotalRevenueByMerchant(Long merchantId) {
        Double revenue = orderRepository.findTotalRevenueByMerchant(merchantId);
        return revenue != null ? revenue : 0.0;
    }

    public long countPendingOrdersByMerchant(Long merchantId) {
        Long count = orderRepository.countPendingOrdersByMerchant(merchantId);
        return count != null ? count : 0L;
    }

    public List<Object[]> getSalesLast7Days(Long merchantId) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        return orderRepository.findSalesLast7Days(merchantId, startDate);
    }

    public List<Object[]> getSalesByCategory(Long merchantId) {
        return orderRepository.findSalesByCategory(merchantId);
    }

    public List<Object[]> getOrderStatusStats(Long merchantId) {
        return orderRepository.findOrderStatusStats(merchantId);
    }

    public List<Object[]> getTopSellingProducts(Long merchantId) {
        return orderRepository.findTopSellingProducts(merchantId);
    }

    // ============================================================
    // 📊 STATISTIQUES GLOBALES POUR L'ADMIN
    // ============================================================

    public double getTotalRevenue() {
        Double revenue = orderRepository.findTotalRevenue();
        return revenue != null ? revenue : 0.0;
    }

    public List<Object[]> getGlobalSalesLast7Days() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        return orderRepository.findGlobalSalesLast7Days(startDate);
    }

    public List<Object[]> getGlobalSalesByCategory() {
        return orderRepository.findGlobalSalesByCategory();
    }

    public List<Object[]> getGlobalOrderStatusStats() {
        return orderRepository.findGlobalOrderStatusStats();
    }

    public List<Object[]> getGlobalTopSellingProducts() {
        return orderRepository.findGlobalTopSellingProducts();
    }
}