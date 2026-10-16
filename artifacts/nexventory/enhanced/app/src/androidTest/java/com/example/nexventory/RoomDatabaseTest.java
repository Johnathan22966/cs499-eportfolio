package com.example.nexventory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.StringReader;

/** Real Room tests use disposable databases; none opens the user's inventory. */
public class RoomDatabaseTest {
    private Context context() {return InstrumentationRegistry.getInstrumentation().getTargetContext();}
    private String name() {return "room-test-"+System.nanoTime()+".db";}

    @Test public void allLegacyVersionsPreserveAccountsIdsAndStock() {
        for(int version=1;version<=3;version++) {
            String name=name();Context c=context();
            try {
                try(SQLiteDatabase old=c.openOrCreateDatabase(name,0,null)) {
                    old.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT,username TEXT UNIQUE NOT NULL,password TEXT NOT NULL)");
                    old.execSQL("CREATE TABLE inventory(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,sku TEXT UNIQUE NOT NULL,quantity INTEGER NOT NULL"+
                            (version==3?",minimum_stock INTEGER NOT NULL DEFAULT 5 CHECK(minimum_stock>=0)":"")+")");
                    old.execSQL("INSERT INTO users(id,username,password) VALUES (7,'legacy',?)",new Object[]{version==1?"test-only-password":PasswordHasher.hash("test-only-password")});
                    old.execSQL("INSERT INTO inventory(id,name,sku,quantity"+(version==3?",minimum_stock":"")+") VALUES (42,'Cable','OLD',3"+(version==3?",9":"")+")");
                    old.execSQL("INSERT INTO inventory(id,name,sku,quantity) VALUES (100,'Deleted','DEL',0)");
                    old.execSQL("DELETE FROM inventory WHERE id=100");old.setVersion(version);
                }
                try(DatabaseHelper helper=new DatabaseHelper(NexventoryDatabase.builder(c,name).build())) {
                    assertTrue(helper.validateUser("legacy","test-only-password"));
                    assertFalse(helper.validateUser("legacy","wrong"));
                    InventoryItem item=helper.getAllInventoryItems().get(0);
                    assertEquals(42,item.getId());assertEquals("OLD",item.getSku());assertEquals(3,item.getQuantity());
                    assertEquals(version==3?9:5,item.getMinimumStock());assertEquals("",item.getCategory());
                    assertTrue(helper.addInventoryItem("Later","NEW",1,2,"Cables","Rack A"));
                    assertTrue(helper.searchInventoryItems("NEW",SearchMode.SKU).get(0).getId()>100);
                }
                try(DatabaseHelper reopened=new DatabaseHelper(NexventoryDatabase.builder(c,name).build())) {
                    assertEquals(2,reopened.getTotalItems());
                    assertEquals("Rack A",reopened.searchInventoryItems("NEW",SearchMode.SKU).get(0).getLocation());
                    assertTrue(reopened.validateUser("legacy","test-only-password"));
                }
            } finally {c.deleteDatabase(name);}
        }
    }

    @Test public void validImportPersistsAndDuplicateOrInvalidBatchWritesNothing() throws Exception {
        String name=name();Context c=context();String h=CsvInventory.HEADER+"\n";
        try {
            try(DatabaseHelper helper=new DatabaseHelper(NexventoryDatabase.builder(c,name).build())) {
                assertEquals(2,helper.importCsv(new StringReader(h+"Cable,C1,1,5,Cables,Rack A\nSwitch,S1,0,1,Switches,Rack B\n")));
                try{helper.importCsv(new StringReader(h+"New,N1,2,4,,\nExisting,C1,3,4,,\n"));fail();}
                catch(IllegalArgumentException expected){assertTrue(expected.getMessage().contains("line 3"));}
                assertEquals(2,helper.getTotalItems());assertFalse(helper.skuExists("N1",-1));
                try{helper.importCsv(new StringReader(h+"New,N2,2,4,,\nBad,B1,-1,4,,\n"));fail();}
                catch(IllegalArgumentException expected){assertTrue(expected.getMessage().contains("line 3"));}
                assertEquals(2,helper.getTotalItems());
                assertFalse(helper.addInventoryItem("Bad","BAD",-1,5));
                assertFalse(helper.addInventoryItem("Duplicate","C1",3,4));
                InventoryItem item=helper.searchInventoryItems("C1",SearchMode.SKU).get(0);
                assertTrue(helper.updateInventoryItem(item.getId(),"Cable","C1",5,5,"Cables","Rack C"));
                assertEquals(1,helper.getRestockCount());
            }
            try(DatabaseHelper reopened=new DatabaseHelper(NexventoryDatabase.builder(c,name).build())) {
                assertEquals("Rack C",reopened.searchInventoryItems("C1",SearchMode.SKU).get(0).getLocation());
                assertEquals(2,reopened.getTotalItems());
                assertTrue(reopened.deleteInventoryItem(reopened.searchInventoryItems("S1",SearchMode.SKU).get(0).getId()));
                assertEquals(1,reopened.getTotalItems());
            }
        } finally {c.deleteDatabase(name);}
    }

    @Test public void unexpectedInsertFailureRollsBackEntireBatchAndRawValuesAreGuarded() throws Exception {
        String name=name();Context c=context();
        try(NexventoryDatabase db=NexventoryDatabase.builder(c,name).build()) {
            DatabaseHelper helper=new DatabaseHelper(db);
            db.getOpenHelper().getWritableDatabase().execSQL("CREATE TRIGGER test_fail BEFORE INSERT ON inventory WHEN NEW.sku='FAIL' BEGIN SELECT RAISE(ABORT,'test failure'); END");
            try{helper.importCsv(new StringReader(CsvInventory.HEADER+"\nFirst,FIRST,1,5,,\nFail,FAIL,1,5,,\n"));fail();}
            catch(IllegalArgumentException expected) {}
            assertEquals(0,helper.getTotalItems());
            try{db.getOpenHelper().getWritableDatabase().execSQL("INSERT INTO inventory(name,sku,quantity) VALUES ('Bad','NEG',-1)");fail();}
            catch(android.database.sqlite.SQLiteException expected) {}
            assertEquals(0,helper.getTotalItems());
        } finally {c.deleteDatabase(name);}
    }
}
