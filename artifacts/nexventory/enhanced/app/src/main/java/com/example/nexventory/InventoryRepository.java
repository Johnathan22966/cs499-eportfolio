package com.example.nexventory;

import android.content.Context;
import java.util.List;

/** One place for screen classes to access inventory records and summaries. */
public final class InventoryRepository {
    private final DatabaseHelper database;

    public InventoryRepository(Context context) {
        database = new DatabaseHelper(context);
    }

    public boolean add(String name, String sku, int quantity, int minimumStock) {
        return database.addInventoryItem(name, sku, quantity, minimumStock);
    }

    public boolean update(int id, String name, String sku, int quantity, int minimumStock) {
        return database.updateInventoryItem(id, name, sku, quantity, minimumStock);
    }

    public boolean add(String n,String s,int q,int m,String c,String l) {
        return database.addInventoryItem(n,s,q,m,c,l);
    }
    public boolean update(int id,String n,String s,int q,int m,String c,String l) {
        return database.updateInventoryItem(id,n,s,q,m,c,l);
    }
    public int importCsv(java.io.Reader reader) throws java.io.IOException {return database.importCsv(reader);}
    public boolean delete(int id) { return database.deleteInventoryItem(id); }
    public int addSampleInventory() { return database.addSampleInventory(); }
    public int removeSampleInventory() { return database.removeSampleInventory(); }
    public boolean skuExists(String sku, int exceptId) { return database.skuExists(sku, exceptId); }
    public List<InventoryItem> search(String query, SearchMode mode) {
        return database.searchInventoryItems(query, mode);
    }
    public List<InventoryItem> select(String query, SearchMode mode,
            InventoryAlgorithms.StockFilter filter, InventoryAlgorithms.SortOrder order) {
        return InventoryAlgorithms.select(search(query, mode), filter, order);
    }
    public int restockCount() { return database.getRestockCount(); }
    public int total() { return database.getTotalItems(); }
    public int lowStock() { return database.getLowStockCount(); }
    public int outOfStock() { return database.getOutOfStockCount(); }
}
