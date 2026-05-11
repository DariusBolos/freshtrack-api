package com.freshtrack.api.recipe.dto;

import jakarta.validation.constraints.NotBlank;

public record RecipeSingleRequest(
        @NotBlank String productName
) {}

