package com.example.nexventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "nexventory.db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_USERS = "users";
    private static final String TABLE_INVENTORY = "inventory";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Stores app users so login information persists after the app closes.
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL)");

        // Stores inventory records that are shown in the database grid.
        db.execSQL("CREATE TABLE " + TABLE_INVENTORY + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "sku TEXT UNIQUE NOT NULL, " +
                "quantity INTEGER NOT NULL CHECK(quantity >= 0), " +
                "minimum_stock INTEGER NOT NULL DEFAULT 5 CHECK(minimum_stock >= 0))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Preserve existing records while replacing legacy plain text passwords.
            List<Integer> ids = new ArrayList<>();
            List<String> passwords = new ArrayList<>();
            try (Cursor cursor = db.rawQuery("SELECT id, password FROM " + TABLE_USERS, null)) {
                while (cursor.moveToNext()) {
                    ids.add(cursor.getInt(0));
                    passwords.add(cursor.getString(1));
                }
            }
            for (int i = 0; i < ids.size(); i++) {
                ContentValues values = new ContentValues();
                values.put("password", PasswordHasher.hash(passwords.get(i)));
                db.update(TABLE_USERS, values, "id = ?", new String[]{String.valueOf(ids.get(i))});
            }
        }
        if (oldVersion < 3) {
            // Preserve inventory and account values; existing items start with target 5.
            db.execSQL("ALTER TABLE " + TABLE_INVENTORY +
                    " ADD COLUMN minimum_stock INTEGER NOT NULL DEFAULT 5 CHECK(minimum_stock >= 0)");
        }
    }

    public boolean createUser(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", PasswordHasher.hash(password));
        try { return db.insertOrThrow(TABLE_USERS, null, values) != -1; }
        catch (SQLiteConstraintException error) { return false; }
    }

    public boolean validateUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT password FROM " + TABLE_USERS + " WHERE username = ?",
                new String[]{username})) {
            return cursor.moveToFirst() && PasswordHasher.matches(password, cursor.getString(0));
        }
    }

    public boolean addInventoryItem(String name, String sku, int quantity, int minimumStock) {
        if (name == null || name.trim().isEmpty() || sku == null || sku.trim().isEmpty() || quantity < 0 || minimumStock < 0) return false;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("sku", sku);
        values.put("quantity", quantity);
        values.put("minimum_stock", minimumStock);
        try { return db.insertOrThrow(TABLE_INVENTORY, null, values) != -1; }
        catch (SQLiteConstraintException error) { return false; }
    }

    public boolean updateInventoryItem(int id, String name, String sku, int quantity, int minimumStock) {
        if (name == null || name.trim().isEmpty() || sku == null || sku.trim().isEmpty() || quantity < 0 || minimumStock < 0) return false;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("sku", sku);
        values.put("quantity", quantity);
        values.put("minimum_stock", minimumStock);
        try {
            return db.update(TABLE_INVENTORY, values, "id = ?", new String[]{String.valueOf(id)}) > 0;
        } catch (SQLiteConstraintException error) {
            return false;
        }
    }

    public boolean deleteInventoryItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_INVENTORY, "id = ?", new String[]{String.valueOf(id)});
        return rowsDeleted > 0;
    }

    public boolean skuExists(String sku, int exceptId) {
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT 1 FROM " + TABLE_INVENTORY + " WHERE sku = ? AND id != ? LIMIT 1",
                new String[]{sku, String.valueOf(exceptId)})) {
            return cursor.moveToFirst();
        }
    }

    public int addSampleInventory() {
        SQLiteDatabase db = getWritableDatabase();
        int added = 0;
        db.beginTransaction();
        try {
            for (String[] sample : SampleInventory.ITEMS) {
                ContentValues values = new ContentValues();
                values.put("name", sample[0]);
                values.put("sku", sample[1]);
                values.put("quantity", Integer.parseInt(sample[2]));
                if (db.insertWithOnConflict(TABLE_INVENTORY, null, values,
                        SQLiteDatabase.CONFLICT_IGNORE) != -1) added++;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return added;
    }

    public int removeSampleInventory() {
        StringBuilder placeholders = new StringBuilder();
        String[] sampleSkus = new String[SampleInventory.ITEMS.length];
        for (int i = 0; i < sampleSkus.length; i++) {
            if (i > 0) placeholders.append(',');
            placeholders.append('?');
            sampleSkus[i] = SampleInventory.ITEMS[i][1];
        }
        return getWritableDatabase().delete(TABLE_INVENTORY,
                "sku IN (" + placeholders + ")", sampleSkus);
    }

    public List<InventoryItem> getAllInventoryItems() {
        return searchInventoryItems("", SearchMode.PRODUCT_NAME);
    }

    public List<InventoryItem> searchInventoryItems(String search, SearchMode mode) {
        List<InventoryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String term = search == null ? "" : search.trim();
        String pattern = "%" + term.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        String where;
        String[] args;
        if (term.isEmpty()) {
            where = "";
            args = null;
        } else if (mode == SearchMode.ITEM_NUMBER) {
            if (!term.matches("[0-9]+")) return items;
            where = " WHERE id = ?";
            args = new String[]{term};
        } else {
            where = mode == SearchMode.SKU ? " WHERE sku LIKE ? ESCAPE '\\'"
                    : " WHERE name LIKE ? ESCAPE '\\'";
            args = new String[]{pattern};
        }
        try (Cursor cursor = db.rawQuery(
                "SELECT id, name, sku, quantity, minimum_stock FROM " + TABLE_INVENTORY
                        + where, args)) {
            while (cursor.moveToNext()) {
                items.add(new InventoryItem(cursor.getInt(0), cursor.getString(1),
                        cursor.getString(2), cursor.getInt(3), cursor.getInt(4)));
            }
        }
        return items;
    }

    public int getTotalItems() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_INVENTORY, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getLowStockCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_INVENTORY + " WHERE quantity > 0 AND quantity < minimum_stock",
                null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getRestockCount() {
        try (Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_INVENTORY + " WHERE quantity < minimum_stock", null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    public int getOutOfStockCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_INVENTORY + " WHERE quantity = 0",
                null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
}
