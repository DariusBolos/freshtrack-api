package com.freshtrack.api.scan.gemini;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.freshtrack.api.enums.ProductCategory;
import com.freshtrack.api.product.dto.ProductRequest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GeminiReceiptClient {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(40);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final int defaultExpiryDays;

    public GeminiReceiptClient(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.model}") String model,
            @Value("${gemini.receipt.default-expiry-days:7}") int defaultExpiryDays
    ) {
        this.apiKey = apiKey == null ? "" : apiKey;
        this.model = model;
        this.defaultExpiryDays = defaultExpiryDays;
        this.httpClient = HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build();
        this.objectMapper = new ObjectMapper();
    }

    public List<ProductRequest> analyzeReceipt(MultipartFile image) {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key is not configured.");
        }
        try {
            String mimeType = image.getContentType() == null ? "image/jpeg" : image.getContentType();
            String base64 = Base64.getEncoder().encodeToString(image.getBytes());
            String requestBody = buildRequestBody(mimeType, base64);

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

            return parseResponse(response.body());
        } catch (IOException ex) {
            throw new IllegalStateException("Could not read receipt image.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gemini request interrupted.", ex);
        }
    }

    private String buildEndpointUrl() {
        return "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
    }

    private String buildRequestBody(String mimeType, String base64) throws IOException {
        String prompt = buildPrompt();
        JsonNode root = objectMapper.createObjectNode()
                .set("contents", objectMapper.createArrayNode()
                        .add(objectMapper.createObjectNode()
                                .put("role", "user")
                                .set("parts", objectMapper.createArrayNode()
                                        .add(objectMapper.createObjectNode().put("text", prompt))
                                        .add(objectMapper.createObjectNode()
                                                .set("inlineData", objectMapper.createObjectNode()
                                                        .put("mimeType", mimeType)
                                                        .put("data", base64)
                                                )
                                        )
                                )
                        )
                );

        ((ObjectNode) root)
                .set("generationConfig", objectMapper.createObjectNode()
                        .put("temperature", 0.1)
                        .put("responseMimeType", "application/json"));

        return objectMapper.writeValueAsString(root);
    }

    private String buildPrompt() {
        return "You are extracting ONLY food-related grocery products from a receipt image. "
                + "Return ONLY valid raw JSON.\n\n"

                + "Do NOT:\n"
                + "- wrap in markdown\n"
                + "- include explanations\n"
                + "- include comments\n"
                + "- include any text before or after the JSON\n\n"

                + "Output must begin with [ and end with ].\n\n"

                + "Strict rules:\n"
                + "- Include ONLY food or drink products intended for consumption\n"
                + "- Exclude non-food items such as bags, discounts, coupons, taxes, deposits, household products, hygiene products, cigarettes, and receipt metadata\n"
                + "- Product names must be formatted with only the first letter of each word capitalized and all other letters lowercase (example: 'Milk', 'Chicken Breast', 'Orange Juice')\n"
                + "- If the original receipt text is uppercase, normalize it\n"
                + "- Preserve meaningful product wording\n\n"

                + "Return ONLY a JSON array with objects matching this schema:\n"
                + "[{\"name\": string, \"quantity\": number, \"unit\": string, "
                + "\"purchaseDate\": \"YYYY-MM-DD\", "
                + "\"expiryDate\": \"YYYY-MM-DD\", "
                + "\"category\": \"dairy|meat|fruit|vegetable|bakery|beverage|frozen|other\"}].\n\n"

                + "Rules for missing values:\n"
                + "- If quantity is missing, use 1.0\n"
                + "- If unit is missing, use 'item'\n"
                + "- If category is unclear, use 'other'\n"
                + "- If purchaseDate is missing, use today's date\n"
                + "- If expiryDate is missing, set it to purchaseDate plus "
                + defaultExpiryDays + " days.";
    }

    private List<ProductRequest> parseResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (textNode.isMissingNode() || textNode.isNull()) {
            throw new IllegalStateException("Gemini response did not include any text output.");
        }

        String rawText = textNode.asText();
        String jsonPayload = extractJsonPayload(rawText);

        JsonNode parsed = objectMapper.readTree(jsonPayload);
        JsonNode itemsNode = parsed;
        if (parsed.isObject()) {
            if (parsed.has("items")) {
                itemsNode = parsed.get("items");
            } else if (parsed.has("products")) {
                itemsNode = parsed.get("products");
            }
        }

        if (!itemsNode.isArray()) {
            throw new IllegalStateException("Gemini output is not a JSON array of items.");
        }

        List<ProductRequest> items = new ArrayList<>();
        for (JsonNode item : itemsNode) {
            String name = readText(item, "name");
            if (name == null || name.isBlank()) {
                continue;
            }

            Double quantity = readDouble(item, "quantity", 1.0);
            String unit = readText(item, "unit");
            if (unit == null || unit.isBlank()) {
                unit = "item";
            }

            LocalDate purchaseDate = readDate(item, "purchaseDate", LocalDate.now());
            LocalDate expiryDate = readDate(item, "expiryDate", null);
            if (expiryDate == null) {
                expiryDate = purchaseDate.plusDays(defaultExpiryDays);
            }

            ProductCategory category = readCategory(item, "category");

            items.add(new ProductRequest(
                    name.trim(),
                    quantity,
                    unit.trim(),
                    purchaseDate,
                    expiryDate,
                    category
            ));
        }

        return items;
    }

    private String extractJsonPayload(String text) {
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

    private String readText(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isTextual() ? value.asText() : null;
    }

    private Double readDouble(JsonNode node, String field, Double defaultValue) {
        JsonNode value = node.path(field);
        if (value.isNumber()) {
            return value.asDouble();
        }
        if (value.isTextual()) {
            try {
                return Double.parseDouble(value.asText());
            } catch (NumberFormatException ex) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private LocalDate readDate(JsonNode node, String field, LocalDate defaultValue) {
        String value = readText(node, field);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private ProductCategory readCategory(JsonNode node, String field) {
        String value = readText(node, field);
        if (value == null || value.isBlank()) {
            return ProductCategory.OTHER;
        }
        try {
            return ProductCategory.fromValue(value.trim());
        } catch (IllegalArgumentException ex) {
            return ProductCategory.OTHER;
        }
    }
}

