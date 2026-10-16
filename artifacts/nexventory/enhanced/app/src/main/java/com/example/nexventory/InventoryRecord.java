package com.example.nexventory;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** The stored record. Stock status remains calculated from quantity and minimum. */
@Entity(tableName = "inventory", indices = {@Index(value = "sku", unique = true)})
public class InventoryRecord {
    @PrimaryKey(autoGenerate = true) public int id;
    @NonNull public String name = "";
    @NonNull public String sku = "";
    public int quantity;
    @ColumnInfo(name = "minimum_stock", defaultValue = "5") public int minimumStock = 5;
    @NonNull @ColumnInfo(defaultValue = "''") public String category = "";
    @NonNull @ColumnInfo(defaultValue = "''") public String location = "";

    public InventoryItem toItem() {
        return new InventoryItem(id, name, sku, quantity, minimumStock, category, location);
    }
}
