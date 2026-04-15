package com.freshtrack.api.product.dto;

import com.freshtrack.api.enums.ProductCategory;

import java.time.LocalDate;

public record ProductResponse(
        Long id,
        String name,
        Double quantity,
        String unit,
        LocalDate purchaseDate,
        LocalDate expiryDate,
        ProductCategory category
) {}

