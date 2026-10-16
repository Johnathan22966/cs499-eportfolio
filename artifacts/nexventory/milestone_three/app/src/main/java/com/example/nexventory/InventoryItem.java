package com.example.nexventory;

public class InventoryItem {
    private final int id;
    private final String name;
    private final String sku;
    private final int quantity;
    private final int minimumStock;

    public InventoryItem(int id, String name, String sku, int quantity) {
        this(id, name, sku, quantity, 5);
    }

    public InventoryItem(int id, String name, String sku, int quantity, int minimumStock) {
        if (quantity < 0 || minimumStock < 0) throw new IllegalArgumentException("Stock values cannot be negative");
        if (name == null || sku == null) throw new IllegalArgumentException("Name and SKU are required");
        this.minimumStock = minimumStock;
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

    public int getMinimumStock() { return minimumStock; }

    /** Both values are nonnegative ints, so this subtraction cannot overflow. */
    public int getShortage() { return Math.max(0, minimumStock - quantity); }

    public boolean needsRestock() { return getShortage() > 0; }

    public String getStatus() {
        if (quantity == 0) {
            return "Out";
        } else if (quantity < minimumStock) {
            return "Low";
        }
        return "Good";
    }
}
