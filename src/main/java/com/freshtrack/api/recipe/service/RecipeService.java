package com.freshtrack.api.recipe.service;

import com.freshtrack.api.product.Product;
import com.freshtrack.api.product.ProductRepository;
import com.freshtrack.api.recipe.Recipe;
import com.freshtrack.api.recipe.RecipeRepository;
import com.freshtrack.api.recipe.dto.RecipeDraft;
import com.freshtrack.api.recipe.dto.RecipeResponse;
import com.freshtrack.api.recipe.gemini.GeminiRecipeClient;
import com.freshtrack.api.user.User;
import com.freshtrack.api.user.service.IUserService;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RecipeService implements IRecipeService {
    private final RecipeRepository recipeRepository;
    private final ProductRepository productRepository;
    private final IUserService userService;
    private final GeminiRecipeClient geminiRecipeClient;

    public RecipeService(RecipeRepository recipeRepository,
                         ProductRepository productRepository,
                         IUserService userService,
                         GeminiRecipeClient geminiRecipeClient) {
        this.recipeRepository = recipeRepository;
        this.productRepository = productRepository;
        this.userService = userService;
        this.geminiRecipeClient = geminiRecipeClient;
    }

    @Override
    public List<RecipeResponse> getRecipes(String authToken) {
        User user = userService.getUserByEmail(authToken);
        return recipeRepository.findAllByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<RecipeResponse> refreshRecipes(String authToken) {
        User user = userService.getUserByEmail(authToken);
        List<Product> products = productRepository.findAllByUserId(user.getId());

        IngredientBuckets buckets = buildIngredientBuckets(products);
        List<RecipeDraft> drafts = geminiRecipeClient.generateRecipes(
                buckets.expiringSoon,
                buckets.additional
        );

        recipeRepository.deleteByUserId(user.getId());

        List<Recipe> saved = drafts.stream()
                .map(draft -> toEntity(draft, user))
                .map(recipeRepository::save)
                .toList();

        return saved.stream().map(this::toResponse).toList();
    }

    @Override
    public RecipeResponse generateSingleRecipe(String authToken, String productName) {
        userService.getUserByEmail(authToken);
        RecipeDraft draft = geminiRecipeClient.generateSingleRecipe(productName);
        return toResponse(draft);
    }

    private IngredientBuckets buildIngredientBuckets(List<Product> products) {
        LocalDate today = LocalDate.now();
        LocalDate soonThreshold = today.plusDays(3);

        Set<String> expiringSoon = new LinkedHashSet<>();
        Set<String> additional = new LinkedHashSet<>();

        for (Product product : products) {
            String name = product.getName() == null ? "" : product.getName().trim();
            if (name.isBlank()) {
                continue;
            }

            if (product.getExpiryDate() != null && !product.getExpiryDate().isAfter(soonThreshold)) {
                expiringSoon.add(name);
            } else {
                additional.add(name);
            }
        }

        return new IngredientBuckets(new ArrayList<>(expiringSoon), new ArrayList<>(additional));
    }

    private Recipe toEntity(RecipeDraft draft, User user) {
        Recipe recipe = new Recipe();
        recipe.setTitle(draft.title());
        recipe.setDescription(draft.description());
        recipe.setDuration(draft.duration());
        recipe.setPrepTime(draft.prepTime());
        recipe.setCookTime(draft.cookTime());
        recipe.setServings(draft.servings());
        recipe.setDifficulty(draft.difficulty());
        recipe.setIngredients(draft.ingredients());
        recipe.setSteps(draft.steps());
        recipe.setIcon(draft.icon());
        recipe.setUser(user);
        return recipe;
    }

    private RecipeResponse toResponse(Recipe recipe) {
        return new RecipeResponse(
                recipe.getId() == null ? null : String.valueOf(recipe.getId()),
                recipe.getTitle(),
                recipe.getDescription(),
                recipe.getDuration(),
                recipe.getPrepTime(),
                recipe.getCookTime(),
                recipe.getServings(),
                recipe.getDifficulty(),
                recipe.getIngredients(),
                recipe.getSteps(),
                recipe.getIcon()
        );
    }

    private RecipeResponse toResponse(RecipeDraft draft) {
        return new RecipeResponse(
                draft.id(),
                draft.title(),
                draft.description(),
                draft.duration(),
                draft.prepTime(),
                draft.cookTime(),
                draft.servings(),
                draft.difficulty(),
                draft.ingredients(),
                draft.steps(),
                draft.icon()
        );
    }

    private static class IngredientBuckets {
        private final List<String> expiringSoon;
        private final List<String> additional;

        private IngredientBuckets(List<String> expiringSoon, List<String> additional) {
            this.expiringSoon = expiringSoon;
            this.additional = additional;
        }
    }
}
