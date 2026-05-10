package com.freshtrack.api.recipe.dto;

import java.util.List;

public record RecipeResponse(
        String id,
        String title,
        String description,
        String duration,
        String prepTime,
        String cookTime,
        Integer servings,
        String difficulty,
        List<String> ingredients,
        List<String> steps,
        String icon
) {}

