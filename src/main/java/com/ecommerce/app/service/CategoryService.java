package com.ecommerce.app.service;

import com.ecommerce.app.model.Category;
import com.ecommerce.app.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));
    }

    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Cette catégorie existe déjà");
        }
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = getCategoryById(id);
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setAttributeNames(categoryDetails.getAttributeNames());
        category.setAttributeValues(categoryDetails.getAttributeValues());
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    // ============================================================
    // 🔥 MÉTHODE POUR INITIALISER LES ATTRIBUTS PAR DÉFAUT
    // ============================================================
    public void initializeDefaultAttributes(Category category) {
        String name = category.getName();
        switch (name) {
            case "Vêtements":
                category.setAttributeNames("Couleur,Taille");
                category.setAttributeValues("Bleu,Rouge,Vert,Noir,Blanc:S,M,L,XL");
                break;
            case "Électronique":
                category.setAttributeNames("Couleur,Capacité");
                category.setAttributeValues("Noir,Blanc,Or,Vert:64GB,128GB,256GB,512GB");
                break;
            case "Chaussures":
                category.setAttributeNames("Couleur,Pointure");
                category.setAttributeValues("Noir,Blanc,Marron,Rouge:36,37,38,39,40,41,42,43,44,45");
                break;
            case "Livres":
                category.setAttributeNames("Format,Langue");
                category.setAttributeValues("Broché,Relié,Numérique:Français,Anglais,Espagnol");
                break;
            case "Meubles":
                category.setAttributeNames("Matière,Couleur,Dimensions");
                category.setAttributeValues("Bois,Métal,Verre,Plastique:Blanc,Noir,Marron,Gris:Petit,Moyen,Grand");
                break;
            default:
                category.setAttributeNames("Couleur");
                category.setAttributeValues("Bleu,Rouge,Vert,Noir");
                break;
        }
    }
}