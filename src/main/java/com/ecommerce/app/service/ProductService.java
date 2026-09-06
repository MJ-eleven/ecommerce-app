package com.ecommerce.app.service;

import com.ecommerce.app.model.Category;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.ProductVariant;
import com.ecommerce.app.repository.OrderRepository;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OrderRepository orderRepository;

    // ============================================================
    // GESTION DES PRODUITS
    // ============================================================

    public List<Product> getAllProducts() {
        return productRepository.findActiveProducts();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));
    }

    @Transactional
    public Product createProduct(Product product, Long categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        product.setCategory(category);
        product.setActive(true);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStockQuantity(productDetails.getStockQuantity());
        product.setImageUrl(productDetails.getImageUrl());
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    // ============================================================
    // RECHERCHE
    // ============================================================

    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public List<Product> searchByKeywordAndCategory(String keyword, Long categoryId) {
        return productRepository.findByNameContainingAndCategoryId(keyword, categoryId);
    }

    public List<Product> getFeaturedProducts() {
        return productRepository.findLatestProducts();
    }

    // ============================================================
    // 🔥 TOP 5 PRODUITS LES PLUS VENDUS
    // ============================================================

    public List<Product> getTop5BestSellers() {
        List<Object[]> results = orderRepository.findTop5BestSellers();
        List<Product> topProducts = new ArrayList<>();

        for (Object[] row : results) {
            Long productId = (Long) row[0];
            try {
                Product product = getProductById(productId);
                if (product != null && product.isActive()) {
                    topProducts.add(product);
                }
                if (topProducts.size() >= 5) break;
            } catch (Exception e) {
                System.out.println("⚠️ Produit non trouvé: " + productId);
            }
        }

        return topProducts;
    }

    // ============================================================
    // GESTION DES VARIANTES
    // ============================================================

    @Transactional
    public ProductVariant addVariant(ProductVariant variant) {
        return productVariantRepository.save(variant);
    }

    public List<ProductVariant> getVariantsByProduct(Long productId) {
        return productVariantRepository.findByProductId(productId);
    }

    public ProductVariant getVariantById(Long id) {
        return productVariantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variante non trouvée"));
    }

    public void deleteVariant(Long id) {
        productVariantRepository.deleteById(id);
    }

    @Transactional
    public void deleteVariantsByProduct(Long productId) {
        List<ProductVariant> variants = getVariantsByProduct(productId);
        productVariantRepository.deleteAll(variants);
    }
}