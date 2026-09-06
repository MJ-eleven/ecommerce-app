package com.ecommerce.app.service;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.Review;
import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductService productService;

    @Transactional
    public Review addReview(Product product, User user, int rating, String comment) {
        // Vérifier si l'utilisateur a déjà noté ce produit
        Review existingReview = reviewRepository.findByProductAndUser(product, user).orElse(null);

        Review review;
        if (existingReview != null) {
            // Mise à jour
            existingReview.setRating(rating);
            existingReview.setComment(comment);
            review = existingReview;
        } else {
            // Nouveau
            review = new Review();
            review.setProduct(product);
            review.setUser(user);
            review.setRating(rating);
            review.setComment(comment);
        }

        Review saved = reviewRepository.save(review);

        // Mettre à jour la note moyenne du produit
        updateProductAverageRating(product);

        return saved;
    }

    public void updateProductAverageRating(Product product) {
        Double avg = reviewRepository.getAverageRatingByProduct(product);
        // Stocker la note moyenne dans Product (ajouter un champ rating)
        // ou calculer dynamiquement
    }

    public double getProductAverageRating(Product product) {
        Double avg = reviewRepository.getAverageRatingByProduct(product);
        return avg != null ? avg : 0.0;
    }

    public long getProductReviewCount(Product product) {
        Long count = reviewRepository.countReviewsByProduct(product);
        return count != null ? count : 0L;
    }

    public List<Review> getReviewsByProduct(Product product) {
        return reviewRepository.findByProductOrderByCreatedAtDesc(product);
    }

    public boolean hasUserReviewed(Product product, User user) {
        return reviewRepository.findByProductAndUser(product, user).isPresent();
    }
}