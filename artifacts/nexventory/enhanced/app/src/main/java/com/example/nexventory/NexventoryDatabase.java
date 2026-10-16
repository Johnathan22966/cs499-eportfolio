package com.example.nexventory;

import android.content.Context;
import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

@Database(entities = {InventoryRecord.class, UserRecord.class}, version = 4, exportSchema = true)
public abstract class NexventoryDatabase extends RoomDatabase implements AutoCloseable {
    public abstract InventoryDao inventory();
    public abstract UserDao users();
    private static volatile NexventoryDatabase instance;

    public static NexventoryDatabase shared(Context context) {
        if (instance == null) synchronized (NexventoryDatabase.class) {
            if (instance == null) instance = builder(context.getApplicationContext(), "nexventory.db").build();
        }
        return instance;
    }

    public static Builder<NexventoryDatabase> builder(Context context, String name) {
        return Room.databaseBuilder(context, NexventoryDatabase.class, name)
                .addMigrations(migrationFrom(1), migrationFrom(2), migrationFrom(3))
                .addCallback(new Callback() {
                    @Override public void onCreate(@NonNull SupportSQLiteDatabase db) { installRules(db); }
                    @Override public void onOpen(@NonNull SupportSQLiteDatabase db) { installRules(db); }
                });
    }

    /** Explicit upgrade paths; invalid legacy stock fails the transaction rather than losing data. */
    public static Migration migrationFrom(final int from) {
        return new Migration(from, 4) {
            @Override public void migrate(@NonNull SupportSQLiteDatabase db) {
                if (from == 1) {
                    List<Integer> ids = new ArrayList<>();
                    List<String> passwords = new ArrayList<>();
                    try (Cursor c = db.query("SELECT id, password FROM users")) {
                        while (c.moveToNext()) { ids.add(c.getInt(0)); passwords.add(c.getString(1)); }
                    }
                    for (int i = 0; i < ids.size(); i++) db.execSQL(
                            "UPDATE users SET password = ? WHERE id = ?",
                            new Object[]{PasswordHasher.hash(passwords.get(i)), ids.get(i)});
                }
                // Capture sequence high-water marks, including IDs of deleted rows.
                long inventorySequence = sequence(db, "inventory");
                long userSequence = sequence(db, "users");
                db.execSQL("CREATE TABLE inventory_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                        + "name TEXT NOT NULL, sku TEXT NOT NULL, quantity INTEGER NOT NULL, "
                        + "minimum_stock INTEGER NOT NULL DEFAULT 5, category TEXT NOT NULL DEFAULT '', "
                        + "location TEXT NOT NULL DEFAULT '')");
                String minimum = from < 3 ? "5" : "minimum_stock";
                try (Cursor c = db.query("SELECT COUNT(*) FROM inventory WHERE quantity < 0 OR " + minimum + " < 0")) {
                    if (c.moveToFirst() && c.getInt(0) != 0)
                        throw new IllegalStateException("Correct negative stock values before upgrading. Existing data was preserved.");
                }
                db.execSQL("INSERT INTO inventory_new(id,name,sku,quantity,minimum_stock) "
                        + "SELECT id,name,sku,quantity," + minimum + " FROM inventory");
                db.execSQL("CREATE TABLE users_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                        + "username TEXT NOT NULL, password TEXT NOT NULL)");
                db.execSQL("INSERT INTO users_new(id,username,password) SELECT id,username,password FROM users");
                db.execSQL("DROP TABLE inventory");
                db.execSQL("ALTER TABLE inventory_new RENAME TO inventory");
                db.execSQL("DROP TABLE users");
                db.execSQL("ALTER TABLE users_new RENAME TO users");
                db.execSQL("UPDATE sqlite_sequence SET seq = MAX(seq, ?) WHERE name = 'inventory'", new Object[]{inventorySequence});
                db.execSQL("UPDATE sqlite_sequence SET seq = MAX(seq, ?) WHERE name = 'users'", new Object[]{userSequence});
                db.execSQL("CREATE UNIQUE INDEX index_inventory_sku ON inventory(sku)");
                db.execSQL("CREATE UNIQUE INDEX index_users_username ON users(username)");
                installRules(db);
            }
        };
    }

    private static long sequence(SupportSQLiteDatabase db, String table) {
        try (Cursor c = db.query("SELECT seq FROM sqlite_sequence WHERE name = ?", new Object[]{table})) {
            return c.moveToFirst() ? c.getLong(0) : 0;
        }
    }

    /** Triggers protect stock values even when a write bypasses the repository. */
    private static void installRules(SupportSQLiteDatabase db) {
        for (String event : new String[]{"INSERT", "UPDATE"}) {
            db.execSQL("CREATE TRIGGER IF NOT EXISTS inventory_valid_" + event.toLowerCase(java.util.Locale.ROOT)
                    + " BEFORE " + event + " ON inventory WHEN "
                    + "typeof(NEW.quantity) != 'integer' OR NEW.quantity < 0 OR NEW.quantity > 2147483647 "
                    + "OR typeof(NEW.minimum_stock) != 'integer' OR NEW.minimum_stock < 0 OR NEW.minimum_stock > 2147483647 "
                    + "OR length(trim(NEW.name)) = 0 OR length(trim(NEW.sku)) = 0 "
                    + "BEGIN SELECT RAISE(ABORT, 'Invalid inventory values'); END");
        }
    }
}
