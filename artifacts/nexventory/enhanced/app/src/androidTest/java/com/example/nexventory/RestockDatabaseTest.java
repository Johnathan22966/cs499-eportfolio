package com.example.nexventory;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.util.List;

/** Uses a uniquely named temporary database, never the user's nexventory.db. */
public class RestockDatabaseTest {
    private Context isolatedContext() {
        Context target=InstrumentationRegistry.getInstrumentation().getTargetContext();
        final File file=new File(target.getCacheDir(),"restock-test-"+System.nanoTime()+".db");
        return new ContextWrapper(target) {
            @Override public Context getApplicationContext() { return this; }
            @Override public File getDatabasePath(String name) { return file; }
            @Override public SQLiteDatabase openOrCreateDatabase(String name,int mode,SQLiteDatabase.CursorFactory factory) {
                return SQLiteDatabase.openOrCreateDatabase(file,factory);
            }
            @Override public SQLiteDatabase openOrCreateDatabase(String name,int mode,SQLiteDatabase.CursorFactory factory,DatabaseErrorHandler handler) {
                return SQLiteDatabase.openOrCreateDatabase(file.getPath(),factory,handler);
            }
            @Override public boolean deleteDatabase(String name) { return SQLiteDatabase.deleteDatabase(file); }
        };
    }
    @Test public void migrationPreservesRecordsAndTargetsPersist() {
        Context context=isolatedContext();
        try {
            try(SQLiteDatabase old=context.openOrCreateDatabase("nexventory.db",0,null)) {
                old.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL)");
                old.execSQL("CREATE TABLE inventory (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, sku TEXT UNIQUE NOT NULL, quantity INTEGER NOT NULL CHECK(quantity >= 0))");
                old.execSQL("INSERT INTO users(username,password) VALUES (?,?)",new Object[]{"tester",PasswordHasher.hash("test-only-pass")});
                old.execSQL("INSERT INTO inventory(name,sku,quantity) VALUES ('Cable','OLD',3)");
                old.setVersion(2);
            }
            try(DatabaseHelper helper=new DatabaseHelper(NexventoryDatabase.builder(context,"nexventory.db").build())) {
                assertTrue(helper.validateUser("tester","test-only-pass"));
                InventoryItem old=helper.getAllInventoryItems().get(0);
                assertEquals(1,old.getId()); assertEquals(3,old.getQuantity()); assertEquals(5,old.getMinimumStock());
                assertTrue(helper.updateInventoryItem(old.getId(),old.getName(),old.getSku(),3,10));
                assertFalse(helper.addInventoryItem("Bad","BAD",1,-1));
                assertTrue(helper.addInventoryItem("No target","NONE",0,0));
                assertEquals(1,helper.getRestockCount());
                assertEquals(1,helper.getLowStockCount());
                assertEquals(1,helper.getOutOfStockCount());
            }
            try(DatabaseHelper reopened=new DatabaseHelper(NexventoryDatabase.builder(context,"nexventory.db").build())) {
                assertEquals(10,reopened.searchInventoryItems("OLD",SearchMode.SKU).get(0).getMinimumStock());
                assertEquals(2,reopened.getTotalItems());
            }
        } finally { context.deleteDatabase("nexventory.db"); }
    }
    @Test public void freshDatabaseSearchAndRestockFiltersCompose() {
        Context context=isolatedContext();
        try(DatabaseHelper helper=new DatabaseHelper(NexventoryDatabase.builder(context,"nexventory.db").build())) {
            assertTrue(helper.addInventoryItem("Adapter","A",0,4));
            assertTrue(helper.addInventoryItem("Patch Cable","C",3,10));
            assertTrue(helper.addInventoryItem("Spare Switch","S",2,3));
            assertTrue(helper.addInventoryItem("Label Roll","L",12,5));
            assertTrue(helper.addInventoryItem("100%_Cable","LITERAL",5,5));
            assertEquals(1,helper.searchInventoryItems("%_",SearchMode.PRODUCT_NAME).size());
            assertEquals(2,helper.searchInventoryItems("cable",SearchMode.PRODUCT_NAME).size());
            assertTrue(helper.searchInventoryItems("missing",SearchMode.SKU).isEmpty());
            assertTrue(helper.searchInventoryItems("invalid",SearchMode.ITEM_NUMBER).isEmpty());
            List<InventoryItem> result=InventoryAlgorithms.select(helper.getAllInventoryItems(),
                    InventoryAlgorithms.StockFilter.NEEDS_RESTOCK,InventoryAlgorithms.SortOrder.RESTOCK_PRIORITY);
            assertEquals(3,result.size()); assertEquals("Adapter",result.get(0).getName());
            assertEquals("Patch Cable",result.get(1).getName()); assertEquals("Spare Switch",result.get(2).getName());
        } finally { context.deleteDatabase("nexventory.db"); }
    }
}
