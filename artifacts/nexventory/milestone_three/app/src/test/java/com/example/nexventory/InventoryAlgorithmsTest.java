package com.example.nexventory;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class InventoryAlgorithmsTest {
    @Test public void plannedRestockExampleAndBoundary() {
        List<InventoryItem> items=Arrays.asList(
                new InventoryItem(2,"Patch Cable","C",3,10),
                new InventoryItem(4,"Label Roll","L",12,5),
                new InventoryItem(1,"Adapter","A",0,4),
                new InventoryItem(3,"Spare Switch","S",2,3),
                new InventoryItem(5,"At target","T",5,5),
                new InventoryItem(6,"No target","N",0,0));
        List<InventoryItem> result=InventoryAlgorithms.select(items,
                InventoryAlgorithms.StockFilter.NEEDS_RESTOCK,InventoryAlgorithms.SortOrder.RESTOCK_PRIORITY);
        assertEquals(3,result.size());
        assertEquals(1,result.get(0).getId());
        assertEquals(2,result.get(1).getId());
        assertEquals(3,result.get(2).getId());
        assertEquals(2,items.get(0).getId());
        assertEquals("Good",items.get(4).getStatus());
        assertFalse(items.get(5).needsRestock());
        assertEquals("Out",items.get(5).getStatus());
    }
    @Test public void tieBreakersAndHeapAgree() {
        List<InventoryItem> data=Arrays.asList(new InventoryItem(3,"beta","B",1,5),
                new InventoryItem(2,"ALPHA","A2",1,5),new InventoryItem(1,"alpha","A1",1,5));
        List<InventoryItem> result=InventoryAlgorithms.restockUsingHeap(data);
        assertEquals(1,result.get(0).getId());
        assertEquals(2,result.get(1).getId());
        assertEquals(3,result.get(2).getId());
        assertTrue(InventoryAlgorithms.restockUsingHeap(Collections.emptyList()).isEmpty());
    }
    @Test(expected=IllegalArgumentException.class) public void overflowingMinimumRejected() {
        InputRules.minimumStock("2147483648");
    }
}
