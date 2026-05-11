package com.freshtrack.api.recipe.gemini;

import com.freshtrack.api.recipe.dto.RecipeDraft;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
public class GeminiRecipeClient {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(40);
    private static final int REQUIRED_RECIPES = 3;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GeminiRecipeClient(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.model}") String model
    ) {
        this.apiKey = apiKey == null ? "" : apiKey;
        this.model = model;
        this.httpClient = HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build();
        this.objectMapper = new ObjectMapper();
    }

    public List<RecipeDraft> generateRecipes(List<String> prioritizedIngredients, List<String> additionalIngredients) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key is not configured.");
        }
        try {
            String requestBody = buildRequestBody(prioritizedIngredients, additionalIngredients);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(buildEndpointUrl()))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Gemini API request failed: " + response.statusCode() + " " + response.body());
            }

            List<RecipeDraft> recipes = parseRecipes(response.body());
            if (recipes.size() < REQUIRED_RECIPES) {
                throw new IllegalStateException("Gemini returned fewer than 3 recipes.");
            }
            return recipes.subList(0, REQUIRED_RECIPES);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not call Gemini API.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gemini request interrupted.", ex);
        }
    }

    public RecipeDraft generateSingleRecipe(String productName) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key is not configured.");
        }
        try {
            String requestBody = buildSingleRecipeRequest(productName);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(buildEndpointUrl()))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Gemini API request failed: " + response.statusCode() + " " + response.body());
            }

            List<RecipeDraft> recipes = parseRecipes(response.body());
            if (recipes.isEmpty()) {
                throw new IllegalStateException("Gemini returned no recipes.");
            }
            return recipes.get(0);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not call Gemini API.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gemini request interrupted.", ex);
        }
    }

    private String buildEndpointUrl() {
        return "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
    }

    private String buildRequestBody(List<String> prioritizedIngredients, List<String> additionalIngredients) throws IOException {
        String prompt = buildPrompt(prioritizedIngredients, additionalIngredients);
        JsonNode root = objectMapper.createObjectNode()
                .set("contents", objectMapper.createArrayNode()
                        .add(objectMapper.createObjectNode()
                                .put("role", "user")
                                .set("parts", objectMapper.createArrayNode()
                                        .add(objectMapper.createObjectNode().put("text", prompt))
                                )
                        )
                );

        ((ObjectNode) root)
                .set("generationConfig", objectMapper.createObjectNode()
                        .put("temperature", 0.4)
                        .put("responseMimeType", "application/json"));

        return objectMapper.writeValueAsString(root);
    }

    private String buildSingleRecipeRequest(String productName) throws IOException {
        String prompt = buildSingleRecipePrompt(productName);
        JsonNode root = objectMapper.createObjectNode()
                .set("contents", objectMapper.createArrayNode()
                        .add(objectMapper.createObjectNode()
                                .put("role", "user")
                                .set("parts", objectMapper.createArrayNode()
                                        .add(objectMapper.createObjectNode().put("text", prompt))
                                )
                        )
                );

        ((ObjectNode) root)
                .set("generationConfig", objectMapper.createObjectNode()
                        .put("temperature", 0.4)
                        .put("responseMimeType", "application/json"));

        return objectMapper.writeValueAsString(root);
    }

    private String buildPrompt(List<String> prioritizedIngredients, List<String> additionalIngredients) {
        String priorityLine = formatIngredients(prioritizedIngredients);
        String additionalLine = formatIngredients(additionalIngredients);

        return "You are a practical home-cooking recipe generator. "
                + "Generate EXACTLY 3 realistic, distinct recipes.\n\n"

                + "Priority ingredients (use first whenever possible): "
                + priorityLine + ".\n"

                + "Additional available ingredients: "
                + additionalLine + ".\n\n"

                + "Ingredient fallback rules:\n"
                + "- If enough pantry ingredients exist, use ONLY those ingredients plus basic staples\n"
                + "- If pantry ingredients are insufficient for 3 complete recipes, generate sensible default recipes using common household ingredients\n"
                + "- Default fallback ingredients may include: chicken, rice, pasta, eggs, potatoes, onions, garlic, tomatoes, cheese, bread, milk\n"
                + "- If no ingredients are provided, generate 3 universally practical recipes using common staples\n"
                + "- Always prioritize provided ingredients over fallback ingredients\n\n"

                + "Strict recipe rules:\n"
                + "- Prioritize expiring ingredients heavily\n"
                + "- Each recipe should use at least 2 priority ingredients if possible\n"
                + "- Recipes must be practical for a home kitchen\n"
                + "- Recipes must be meaningfully different from each other\n"
                + "- Use concise, appealing titles\n"
                + "- Steps must be clear and sequential\n"
                + "- Cooking times must be realistic\n"
                + "- Prefer food-waste reduction when possible\n\n"

                + "ICON RULES:\n"
                + "- The 'icon' field MUST be exactly one of:\n"
                + "apple-alt, bread-slice, carrot, cheese, egg, fish, drumstick-bite, "
                + "pizza-slice, seedling, leaf, pepper-hot, hamburger, utensils, coffee, lemon\n"
                + "- Do NOT output emojis\n"
                + "- Do NOT invent icon names\n\n"

                + "Return ONLY valid raw JSON.\n"
                + "Do NOT:\n"
                + "- wrap in markdown\n"
                + "- include explanations\n"
                + "- include comments\n"
                + "- include text before or after JSON\n\n"

                + "Output must begin with [ and end with ].\n\n"

                + "Schema:\n"
                + "[{\"id\":\"1\","
                + "\"title\":string,"
                + "\"description\":string,"
                + "\"duration\":string,"
                + "\"prepTime\":string,"
                + "\"cookTime\":string,"
                + "\"servings\":number,"
                + "\"difficulty\":\"easy|medium|hard\","
                + "\"ingredients\":[string],"
                + "\"steps\":[string],"
                + "\"icon\":string}]\n\n"

                + "Formatting rules:\n"
                + "- duration, prepTime, cookTime format: '15 min'\n"
                + "- servings must be integer\n"
                + "- steps should contain 4–8 concise instructions\n"
                + "- description must be one short sentence";
    }

    private String buildSingleRecipePrompt(String productName) {
        String product = productName == null ? "" : productName.trim();

        return "You are a practical home-cooking recipe generator. "
                + "Generate EXACTLY 1 realistic recipe.\n\n"

                + "Main ingredient: " + product + ".\n\n"

                + "Ingredient rules:\n"
                + "- The provided product MUST be the primary ingredient\n"
                + "- If the product is missing or empty, generate a practical default recipe using common household ingredients\n"
                + "- Allowed fallback ingredients: chicken, rice, pasta, eggs, potatoes, onions, garlic, tomatoes, cheese, bread, milk\n"
                + "- Additional ingredients may include only common pantry staples: salt, pepper, oil, butter, water\n"
                + "- The recipe must be realistic for a home kitchen\n\n"

                + "Recipe rules:\n"
                + "- Use a concise, appealing title\n"
                + "- Description must be one short sentence\n"
                + "- Steps must be clear, sequential, and easy to follow\n"
                + "- Include 4–8 concise steps\n"
                + "- Cooking times must be realistic\n\n"

                + "ICON RULES:\n"
                + "- The 'icon' field MUST be exactly one of:\n"
                + "apple-alt, bread-slice, carrot, cheese, egg, fish, drumstick-bite, "
                + "pizza-slice, seedling, leaf, pepper-hot, hamburger, utensils, coffee, lemon\n"
                + "- Do NOT output emojis\n"
                + "- Do NOT invent icon names\n\n"

                + "Return ONLY valid raw JSON.\n"
                + "Do NOT:\n"
                + "- wrap in markdown\n"
                + "- include explanations\n"
                + "- include comments\n"
                + "- include text before or after JSON\n\n"

                + "Output must begin with [ and end with ].\n\n"

                + "Return EXACTLY this schema:\n"
                + "[{\"id\":\"1\","
                + "\"title\":string,"
                + "\"description\":string,"
                + "\"duration\":string,"
                + "\"prepTime\":string,"
                + "\"cookTime\":string,"
                + "\"servings\":number,"
                + "\"difficulty\":\"easy|medium|hard\","
                + "\"ingredients\":[string],"
                + "\"steps\":[string],"
                + "\"icon\":string}]\n\n"

                + "Formatting rules:\n"
                + "- duration, prepTime, cookTime format: '15 min'\n"
                + "- servings must be integer";
    }

    private String formatIngredients(List<String> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return "none";
        }
        return String.join(", ", ingredients);
    }

    public static List<RecipeDraft> parseRecipes(String responseBody) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseBody);
        JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (textNode.isMissingNode() || textNode.isNull()) {
            throw new IllegalStateException("Gemini response did not include any text output.");
        }

        String rawText = textNode.asText();
        String jsonPayload = extractJsonPayload(rawText);
        JsonNode parsed = mapper.readTree(jsonPayload);
        if (!parsed.isArray()) {
            throw new IllegalStateException("Gemini output is not a JSON array of recipes.");
        }

        List<RecipeDraft> recipes = new ArrayList<>();
        for (JsonNode recipeNode : parsed) {
            recipes.add(new RecipeDraft(
                    readText(recipeNode, "id"),
                    readText(recipeNode, "title"),
                    readText(recipeNode, "description"),
                    readText(recipeNode, "duration"),
                    readText(recipeNode, "prepTime"),
                    readText(recipeNode, "cookTime"),
                    readInt(recipeNode, "servings"),
                    readText(recipeNode, "difficulty"),
                    readStringList(recipeNode, "ingredients"),
                    readStringList(recipeNode, "steps"),
                    readText(recipeNode, "icon")
            ));
        }

        return recipes;
    }

    private static String extractJsonPayload(String text) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline != -1) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            int fenceIndex = trimmed.lastIndexOf("```");
            if (fenceIndex != -1) {
                trimmed = trimmed.substring(0, fenceIndex).trim();
            }
        }

        int arrayStart = trimmed.indexOf('[');
        int arrayEnd = trimmed.lastIndexOf(']');
        if (arrayStart != -1 && arrayEnd != -1 && arrayEnd > arrayStart) {
            return trimmed.substring(arrayStart, arrayEnd + 1);
        }

        return trimmed;
    }

    private static String readText(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isTextual() ? value.asText() : null;
    }

    private static Integer readInt(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isNumber()) {
            return value.asInt();
        }
        if (value.isTextual()) {
            try {
                return Integer.parseInt(value.asText());
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private static List<String> readStringList(JsonNode node, String field) {
        List<String> values = new ArrayList<>();
        JsonNode arrayNode = node.path(field);
        if (!arrayNode.isArray()) {
            return values;
        }
        for (JsonNode value : arrayNode) {
            if (value.isTextual()) {
                values.add(value.asText());
            }
        }
        return values;
    }
}
