package com.freshtrack.api.product.dto;

import com.freshtrack.api.enums.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ProductRequest(
        @NotBlank(message = "Product name is required") String name,
        @NotNull(message = "Quantity is required") Double quantity,
        @NotBlank(message = "Unit is required") String unit,
        @NotNull(message = "Purchase date is required") LocalDate purchaseDate,
        @NotNull(message = "Expiry date is required") LocalDate expiryDate,
        @NotNull(message = "Category is required") ProductCategory category
) {}

