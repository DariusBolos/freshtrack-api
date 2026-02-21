package com.freshtrack.api.product;

import com.freshtrack.api.enums.ProductCategory;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.Date;

@Data
@Table(name = "products")
public class Product {
    private Long id;
    private String name;
    private ProductCategory category;
    private Double price;
    private Date expirationDate;
    private String imageUrl;

    public Product() {}

    public Product(Long id, String name, ProductCategory category, Double price, Date expirationDate, String imageUrl) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.expirationDate = expirationDate;
        this.imageUrl = imageUrl;
    }
}
