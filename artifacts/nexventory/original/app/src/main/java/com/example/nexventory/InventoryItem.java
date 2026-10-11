package com.example.nexventory;

public class InventoryItem {
    private final int id;
    private final String name;
    private final String sku;
    private final int quantity;

    public InventoryItem(int id, String name, String sku, int quantity) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStatus() {
        if (quantity == 0) {
            return "Out";
        } else if (quantity <= 5) {
            return "Low";
        }
        return "Good";
    }
}
