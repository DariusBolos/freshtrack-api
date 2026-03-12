package com.freshtrack.api.product.service;

import com.freshtrack.api.product.Product;

import java.util.List;

public interface IProductService {
    Product createProduct(Product product);
    Product getProductById(Long id);
    List<Product> getAllProductsByUser(Long userId);
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id);
}

