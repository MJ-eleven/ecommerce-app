package com.ecommerce.app.service;

import com.ecommerce.app.model.Favorite;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.FavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Transactional
    public Favorite addFavorite(User user, Product product) {
        if (favoriteRepository.existsByUserAndProduct(user, product)) {
            throw new RuntimeException("Déjà dans les favoris");
        }
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setProduct(product);
        return favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(User user, Product product) {
        favoriteRepository.deleteByUserAndProduct(user, product);
    }

    public List<Favorite> getFavoritesByUser(User user) {
        return favoriteRepository.findByUser(user);
    }

    public List<Product> getFavoriteProductsByUser(User user) {
        return favoriteRepository.findByUser(user).stream()
                .map(Favorite::getProduct)
                .collect(Collectors.toList());
    }

    public boolean isFavorite(User user, Product product) {
        if (user == null) return false;
        return favoriteRepository.existsByUserAndProduct(user, product);
    }

    public long getFavoriteCountByProduct(Product product) {
        return favoriteRepository.countByProduct(product);
    }
}