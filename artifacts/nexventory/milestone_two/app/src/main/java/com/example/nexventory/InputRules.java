package com.example.nexventory;

public final class InputRules {
    private InputRules() {}

    public static int quantity(String value) {
        if (value == null || !value.matches("[0-9]+")) {
            throw new IllegalArgumentException("Quantity must be a whole number");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("Quantity is too large", error);
        }
    }

    public static String required(String value, String label) {
        String clean = value == null ? "" : value.trim();
        if (clean.isEmpty()) throw new IllegalArgumentException(label + " is required");
        return clean;
    }
}
