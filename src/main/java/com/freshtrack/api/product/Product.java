package com.freshtrack.api.product;

import com.freshtrack.api.enums.ProductCategory;
import com.freshtrack.api.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Double quantity;
    private String unit;
    private LocalDate purchaseDate;
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Product() {}

    public Product(Long id, String name, Double quantity, String unit, LocalDate purchaseDate, LocalDate expiryDate, ProductCategory category, User user) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.category = category;
        this.user = user;
    }
}
