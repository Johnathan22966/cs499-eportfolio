package com.example.nexventory;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

/** Room checks these queries when compiling. All values use bound parameters. */
@Dao
public interface InventoryDao {
    @Insert long insert(InventoryRecord record);
    @Update int update(InventoryRecord record);
    @Query("DELETE FROM inventory WHERE id = :id") int delete(int id);
    @Query("DELETE FROM inventory WHERE sku IN (:skus)") int removeSamples(String[] skus);
    @Query("SELECT * FROM inventory ORDER BY id") List<InventoryRecord> all();
    @Query("SELECT * FROM inventory WHERE id = :id") List<InventoryRecord> byId(int id);
    @Query("SELECT * FROM inventory WHERE name LIKE :pattern ESCAPE '\\' ORDER BY id")
    List<InventoryRecord> byName(String pattern);
    @Query("SELECT * FROM inventory WHERE sku LIKE :pattern ESCAPE '\\' ORDER BY id")
    List<InventoryRecord> bySku(String pattern);
    @Query("SELECT COUNT(*) FROM inventory WHERE sku = :sku AND id != :exceptId")
    int skuCount(String sku, int exceptId);
    @Query("SELECT COUNT(*) FROM inventory") int total();
    @Query("SELECT COUNT(*) FROM inventory WHERE quantity > 0 AND quantity < minimum_stock") int low();
    @Query("SELECT COUNT(*) FROM inventory WHERE quantity = 0") int out();
    @Query("SELECT COUNT(*) FROM inventory WHERE quantity < minimum_stock") int restock();
}
