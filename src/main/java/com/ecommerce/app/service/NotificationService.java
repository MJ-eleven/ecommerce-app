package com.ecommerce.app.service;

import com.ecommerce.app.model.Notification;
import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public Notification createNotification(User user, Order order, String message, String type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setOrder(order);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification createNewOrderNotification(User merchant, Order order) {
        String message = "📦 Nouvelle commande #" + order.getId() + " de " + order.getCustomerName() +
                " pour un montant de " + order.getTotalAmount() + " €";
        return createNotification(merchant, order, message, "NEW_ORDER");
    }

    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndReadOrderByCreatedAtDesc(user, false);
    }

    public List<Notification> getAllNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countUnreadByUser(user);
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user);
    }

    @Transactional
    public void markAsRead(Long notificationId, User user) {
        notificationRepository.markAsRead(notificationId, user);
    }

    public boolean hasUnreadNotifications(User user) {
        return getUnreadCount(user) > 0;
    }
}