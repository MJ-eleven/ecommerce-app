package com.ecommerce.app.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    // 🔥 NOUVEAU : Attributs de la catégorie (séparés par des virgules)
    @Column(name = "attribute_names")
    private String attributeNames;  // Ex: "Couleur,Taille"

    // 🔥 NOUVEAU : Valeurs possibles pour chaque attribut
    @Column(name = "attribute_values")
    private String attributeValues;  // Ex: "Bleu,Rouge,Vert:S,M,L"

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    public Category() {}

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // ============================================================
    // GETTERS ET SETTERS
    // ============================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    public String getAttributeNames() { return attributeNames; }
    public void setAttributeNames(String attributeNames) { this.attributeNames = attributeNames; }

    public String getAttributeValues() { return attributeValues; }
    public void setAttributeValues(String attributeValues) { this.attributeValues = attributeValues; }

    // ============================================================
    // MÉTHODES UTILITAIRES
    // ============================================================

    public String[] getAttributeNamesArray() {
        if (attributeNames == null || attributeNames.isEmpty()) {
            return new String[]{};
        }
        return attributeNames.split(",");
    }

    public String[][] getAttributeValuesArray() {
        if (attributeValues == null || attributeValues.isEmpty()) {
            return new String[][]{};
        }
        String[] parts = attributeValues.split(":");
        String[][] result = new String[parts.length][];
        for (int i = 0; i < parts.length; i++) {
            result[i] = parts[i].split(",");
        }
        return result;
    }
}