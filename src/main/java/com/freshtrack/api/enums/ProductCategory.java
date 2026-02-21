package com.freshtrack.api.enums;

public enum ProductCategory {
    FRUITS("Fruits"),
    VEGETABLES("Vegetables"),
    DAIRY("Dairy"),
    MEAT("Meat"),
    SEAFOOD("Seafood"),
    BAKERY("Bakery"),
    BEVERAGES("Beverages"),
    SNACKS("Snacks"),
    FROZEN_FOODS("Frozen Foods"),
    OTHER("Other");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
