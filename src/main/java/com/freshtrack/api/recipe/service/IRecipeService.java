package com.freshtrack.api.recipe.service;

import com.freshtrack.api.recipe.dto.RecipeResponse;
import java.util.List;

public interface IRecipeService {
    List<RecipeResponse> getRecipes(String authToken);
    List<RecipeResponse> refreshRecipes(String authToken);
    RecipeResponse generateSingleRecipe(String authToken, String productName);
}
