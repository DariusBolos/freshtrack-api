package com.freshtrack.api.recipe;

import com.freshtrack.api.recipe.dto.RecipeDraft;
import com.freshtrack.api.recipe.gemini.GeminiRecipeClient;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeminiRecipeClientTest {
    @Test
    void parseRecipes_extractsThreeItems() throws Exception {
        String payload = "[{" +
                "\\\"id\\\":\\\"1\\\",\\\"title\\\":\\\"Toast\\\",\\\"description\\\":\\\"Desc\\\",\\\"duration\\\":\\\"10 min\\\",\\\"prepTime\\\":\\\"2 min\\\",\\\"cookTime\\\":\\\"8 min\\\",\\\"servings\\\":2,\\\"difficulty\\\":\\\"easy\\\",\\\"ingredients\\\":[\\\"Bread\\\"],\\\"steps\\\":[\\\"Toast bread\\\"],\\\"icon\\\":\\\"bread-slice\\\"}," +
                "{\\\"id\\\":\\\"2\\\",\\\"title\\\":\\\"Salad\\\",\\\"description\\\":\\\"Desc\\\",\\\"duration\\\":\\\"5 min\\\",\\\"prepTime\\\":\\\"5 min\\\",\\\"cookTime\\\":\\\"0 min\\\",\\\"servings\\\":1,\\\"difficulty\\\":\\\"easy\\\",\\\"ingredients\\\":[\\\"Lettuce\\\"],\\\"steps\\\":[\\\"Mix\\\"],\\\"icon\\\":\\\"leaf\\\"}," +
                "{\\\"id\\\":\\\"3\\\",\\\"title\\\":\\\"Pasta\\\",\\\"description\\\":\\\"Desc\\\",\\\"duration\\\":\\\"20 min\\\",\\\"prepTime\\\":\\\"5 min\\\",\\\"cookTime\\\":\\\"15 min\\\",\\\"servings\\\":2,\\\"difficulty\\\":\\\"medium\\\",\\\"ingredients\\\":[\\\"Pasta\\\"],\\\"steps\\\":[\\\"Boil\\\"],\\\"icon\\\":\\\"bowl-food\\\"}" +
                "]";

        String response = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"" + payload + "\"}]}}]}";

        List<RecipeDraft> drafts = GeminiRecipeClient.parseRecipes(response);
        assertEquals(3, drafts.size());
    }
}
