package com.freshtrack.api.recipe;

import com.freshtrack.api.recipe.dto.RecipeResponse;
import com.freshtrack.api.recipe.dto.RecipeSingleRequest;
import com.freshtrack.api.recipe.service.IRecipeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipe-service")
public class RecipeController {
    private final IRecipeService recipeService;

    public RecipeController(IRecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/recipes")
    public ResponseEntity<List<RecipeResponse>> getRecipes(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        return ResponseEntity.ok(recipeService.getRecipes(token));
    }

    @PostMapping("/recipes/refresh")
    public ResponseEntity<List<RecipeResponse>> refreshRecipes(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        return ResponseEntity.ok(recipeService.refreshRecipes(token));
    }

    @PostMapping("/recipes/single")
    public ResponseEntity<RecipeResponse> generateSingleRecipe(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody RecipeSingleRequest request
    ) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        return ResponseEntity.ok(recipeService.generateSingleRecipe(token, request.productName()));
    }
}
