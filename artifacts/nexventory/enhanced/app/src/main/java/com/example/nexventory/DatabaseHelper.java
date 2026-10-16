package com.example.nexventory;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import java.io.*;
import java.util.*;
/** Compatibility boundary; all application storage now uses Room DAOs. */
public final class DatabaseHelper implements AutoCloseable {
    private final NexventoryDatabase database;
    private final boolean ownsDatabase;
    public DatabaseHelper(Context context) {database=NexventoryDatabase.shared(context);ownsDatabase=false;}
    public DatabaseHelper(NexventoryDatabase database) {this.database=database;ownsDatabase=true;}
    public void close() {if(ownsDatabase)database.close();}
    public boolean createUser(String username,String password) {
        if(username==null||username.trim().isEmpty()||password==null||password.isEmpty())return false;
        UserRecord user=new UserRecord();user.username=username.trim();user.password=PasswordHasher.hash(password);
        try{return database.users().insert(user)>0;}catch(SQLiteConstraintException e){return false;}
    }
    public boolean validateUser(String username,String password) {
        if(username==null||password==null)return false;
        String stored=database.users().passwordFor(username.trim());
        return stored!=null&&PasswordHasher.matches(password,stored);
    }
    private static InventoryRecord record(int id,String name,String sku,int quantity,int minimum,String category,String location) {
        InventoryRecord r=new InventoryRecord();r.id=id;
        r.name=InputRules.required(name,"Name");r.sku=InputRules.required(sku,"SKU");
        if(quantity<0||minimum<0)throw new IllegalArgumentException("Stock cannot be negative");
        r.quantity=quantity;r.minimumStock=minimum;r.category=category==null?"":category.trim();r.location=location==null?"":location.trim();
        return r;
    }
    public boolean addInventoryItem(String n,String s,int q,int m){return addInventoryItem(n,s,q,m,"","");}
    public boolean addInventoryItem(String n,String s,int q,int m,String c,String l){
        try{return database.inventory().insert(record(0,n,s,q,m,c,l))>0;}
        catch(SQLiteConstraintException|IllegalArgumentException e){return false;}
    }
    public boolean updateInventoryItem(int id,String n,String s,int q,int m){
        List<InventoryRecord> old=database.inventory().byId(id);if(old.isEmpty())return false;
        return updateInventoryItem(id,n,s,q,m,old.get(0).category,old.get(0).location);
    }
    public boolean updateInventoryItem(int id,String n,String s,int q,int m,String c,String l){
        try{return database.inventory().update(record(id,n,s,q,m,c,l))>0;}
        catch(SQLiteConstraintException|IllegalArgumentException e){return false;}
    }
    public boolean deleteInventoryItem(int id){return database.inventory().delete(id)>0;}
    public boolean skuExists(String s,int exceptId){return database.inventory().skuCount(s,exceptId)>0;}
    public int addSampleInventory(){
        return database.runInTransaction(()->{
            int added=0;for(String[] s:SampleInventory.ITEMS)if(!skuExists(s[1],-1)){
                database.inventory().insert(record(0,s[0],s[1],Integer.parseInt(s[2]),5,"Networking supplies",""));added++;
            }return added;
        });
    }
    public int removeSampleInventory(){
        String[] skus=new String[SampleInventory.ITEMS.length];
        for(int i=0;i<skus.length;i++)skus[i]=SampleInventory.ITEMS[i][1];
        return database.inventory().removeSamples(skus);
    }
    /** Existing-SKU validation and inserts share one transaction to prevent partial imports. */
    public int importCsv(Reader reader)throws IOException{
        List<CsvInventory.Row> rows=CsvInventory.parse(reader);
        try{return database.runInTransaction(()->{
            for(CsvInventory.Row r:rows)if(skuExists(r.sku,-1))
                throw new IllegalArgumentException("CSV line "+r.line+": sku "+r.sku+" already exists. Nothing was imported.");
            for(CsvInventory.Row r:rows)database.inventory().insert(record(0,r.name,r.sku,r.quantity,r.minimumStock,r.category,r.location));
            return rows.size();
        });}catch(SQLiteConstraintException e){throw new IllegalArgumentException("Import conflicts with saved inventory. Nothing was imported.",e);}
    }
    public List<InventoryItem> getAllInventoryItems(){return searchInventoryItems("",SearchMode.PRODUCT_NAME);}
    public List<InventoryItem> searchInventoryItems(String search,SearchMode mode){
        String term=search==null?"":search.trim();List<InventoryRecord> records;
        if(term.isEmpty())records=database.inventory().all();
        else if(mode==SearchMode.ITEM_NUMBER){
            try{records=database.inventory().byId(InputRules.quantity(term));}catch(IllegalArgumentException e){return new ArrayList<>();}
        }else{
            String pattern="%"+term.replace("\\","\\\\").replace("%","\\%").replace("_","\\_")+"%";
            records=mode==SearchMode.SKU?database.inventory().bySku(pattern):database.inventory().byName(pattern);
        }
        List<InventoryItem> items=new ArrayList<>();for(InventoryRecord r:records)items.add(r.toItem());return items;
    }
    public int getTotalItems(){return database.inventory().total();}
    public int getLowStockCount(){return database.inventory().low();}
    public int getOutOfStockCount(){return database.inventory().out();}
    public int getRestockCount(){return database.inventory().restock();}
}
