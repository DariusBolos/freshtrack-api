package com.freshtrack.api.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProductCategory {
    DAIRY("dairy"),
    MEAT("meat"),
    FRUIT("fruit"),
    VEGETABLE("vegetable"),
    BAKERY("bakery"),
    BEVERAGE("beverage"),
    FROZEN("frozen"),
    OTHER("other");

    private final String value;

    ProductCategory(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ProductCategory fromValue(String value) {
        for (ProductCategory category : values()) {
            if (category.value.equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + value);
    }
}
