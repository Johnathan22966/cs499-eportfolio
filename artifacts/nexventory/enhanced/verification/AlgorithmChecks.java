package com.example.nexventory;

import java.util.*;

/** Dependency-free executable regression tests. Throws on any failure. */
public final class AlgorithmChecks {
    private static int checks;
    private static InventoryItem item(int id, String name, int qty, int min) {
        return new InventoryItem(id, name, "SKU-" + id, qty, min);
    }
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    private static void ids(List<InventoryItem> actual, int... expected) {
        check(actual.size() == expected.length, "Result size");
        for (int i = 0; i < expected.length; i++) check(actual.get(i).getId() == expected[i], "Order at " + i);
    }
    private static List<InventoryItem> restock(List<InventoryItem> items) {
        return InventoryAlgorithms.select(items, InventoryAlgorithms.StockFilter.NEEDS_RESTOCK,
                InventoryAlgorithms.SortOrder.RESTOCK_PRIORITY);
    }
    public static void main(String[] args) {
        ids(restock(Collections.emptyList()));
        List<InventoryItem> planned = Arrays.asList(item(4,"Label Roll",12,5), item(3,"Spare Switch",2,3),
                item(2,"Patch Cable",3,10), item(1,"Adapter",0,4));
        ids(restock(planned),1,2,3);
        ids(planned,4,3,2,1); // Sorting must not change the caller's list.
        check(planned.get(2).getShortage()==7,"Shortage");
        ids(restock(Arrays.asList(item(1,"At target",5,5),item(2,"No target",0,0),item(3,"Above",6,5))));
        check(item(1,"At target",5,5).getStatus().equals("Good"),"Exact minimum is good");
        check(item(1,"Low",4,5).getStatus().equals("Low"),"Below minimum is low");
        check(item(1,"Out without target",0,0).getStatus().equals("Out"),"Physical out differs from reorder");
        ids(restock(Arrays.asList(item(9,"beta",1,6),item(5,"ALPHA",1,6),item(2,"alpha",1,6))),2,5,9);
        ids(restock(Arrays.asList(item(1,"Huge shortage",1,Integer.MAX_VALUE),item(2,"Out",0,1))),2,1);
        check(item(1,"Maximum",0,Integer.MAX_VALUE).getShortage()==Integer.MAX_VALUE,"Maximum shortage");
        check(item(1,"No overflow",Integer.MAX_VALUE,0).getShortage()==0,"No overflow");
        check(InputRules.minimumStock("0")==0,"Zero minimum valid");
        check(InputRules.minimumStock("2147483647")==Integer.MAX_VALUE,"Largest minimum valid");
        for(String value:new String[]{null,"","-1","1.5","2147483648","nope"}) {
            boolean rejected=false;
            try { InputRules.minimumStock(value); } catch(IllegalArgumentException e) { rejected=true; }
            check(rejected,"Reject invalid minimum " + value);
        }
        boolean rejected=false;
        try { item(1,"Bad",-1,5); } catch(IllegalArgumentException e) { rejected=true; }
        check(rejected,"Model rejects negative quantity");
        rejected=false;
        try { item(1,"Bad",1,-5); } catch(IllegalArgumentException e) { rejected=true; }
        check(rejected,"Model rejects negative minimum");
        List<InventoryItem> mixed=Arrays.asList(item(1,"z",0,0),item(2,"b",1,5),item(3,"a",5,5),item(4,"c",0,4));
        ids(InventoryAlgorithms.select(mixed,InventoryAlgorithms.StockFilter.OUT,InventoryAlgorithms.SortOrder.NAME),4,1);
        ids(InventoryAlgorithms.select(mixed,InventoryAlgorithms.StockFilter.LOW,InventoryAlgorithms.SortOrder.NAME),2);
        ids(InventoryAlgorithms.select(mixed,InventoryAlgorithms.StockFilter.GOOD,InventoryAlgorithms.SortOrder.NAME),3);
        ids(InventoryAlgorithms.select(mixed,InventoryAlgorithms.StockFilter.ALL,InventoryAlgorithms.SortOrder.QUANTITY),4,1,2,3);
        ids(InventoryAlgorithms.select(mixed,InventoryAlgorithms.StockFilter.ALL,InventoryAlgorithms.SortOrder.RESTOCK_PRIORITY),4,2,3,1);
        Random random=new Random(499);
        for(int trial=0;trial<100;trial++) {
            List<InventoryItem> data=new ArrayList<>();
            for(int i=0;i<200;i++) data.add(item(i,"Part " + random.nextInt(10),random.nextInt(20),random.nextInt(20)));
            List<InventoryItem> sorted=restock(data), heap=InventoryAlgorithms.restockUsingHeap(data);
            check(sorted.size()==heap.size(),"Heap equivalence size");
            Set<Integer> selected=new HashSet<>();
            for(int i=0;i<sorted.size();i++) {
                InventoryItem a=sorted.get(i);
                check(a.needsRestock(),"Only eligible records");
                check(selected.add(a.getId()),"Unique results");
                check(a.getId()==heap.get(i).getId(),"Heap order equivalence");
                if(i>0) check(InventoryAlgorithms.BY_RESTOCK_PRIORITY.compare(sorted.get(i-1),a)<=0,"Monotonic priority");
            }
            Collections.shuffle(data,random);
            List<InventoryItem> shuffled=restock(data);
            for(int i=0;i<sorted.size();i++) check(sorted.get(i).getId()==shuffled.get(i).getId(),"Deterministic across input order");
            for(int i=0;i<100;i++) {
                InventoryItem a=data.get(random.nextInt(200)), b=data.get(random.nextInt(200)), c=data.get(random.nextInt(200));
                Comparator<InventoryItem> cmp=InventoryAlgorithms.BY_RESTOCK_PRIORITY;
                check(Integer.signum(cmp.compare(a,b))==-Integer.signum(cmp.compare(b,a)),"Comparator symmetry");
                if(cmp.compare(a,b)<=0 && cmp.compare(b,c)<=0) check(cmp.compare(a,c)<=0,"Comparator transitivity");
            }
        }
        System.out.println("PASS: " + checks + " assertions; planned example, boundaries, filters, sorting, input, 100 randomized datasets.");
    }
}
