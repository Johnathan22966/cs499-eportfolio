package com.example.nexventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/** Pure inventory rules, independent of Android and persistence. Never mutates input. */
public final class InventoryAlgorithms {
    private InventoryAlgorithms() {}

    public enum StockFilter { ALL, NEEDS_RESTOCK, OUT, LOW, GOOD }
    public enum SortOrder { NAME, QUANTITY, RESTOCK_PRIORITY }

    // Case-insensitive name order; stable database ID resolves equal names.
    public static final Comparator<InventoryItem> BY_NAME = (a, b) -> {
        int result = a.getName().compareToIgnoreCase(b.getName());
        return result != 0 ? result : Integer.compare(a.getId(), b.getId());
    };
    public static final Comparator<InventoryItem> BY_QUANTITY = (a, b) -> {
        int result = Integer.compare(a.getQuantity(), b.getQuantity());
        return result != 0 ? result : BY_NAME.compare(a, b);
    };
    public static final Comparator<InventoryItem> BY_RESTOCK_PRIORITY = (a, b) -> {
        // Candidates first, then out-of-stock candidates, then descending shortage.
        int result = Boolean.compare(b.needsRestock(), a.needsRestock());
        if (result != 0) return result;
        if (a.needsRestock()) {
            result = Boolean.compare(b.getQuantity() == 0, a.getQuantity() == 0);
            if (result != 0) return result;
            result = Integer.compare(b.getShortage(), a.getShortage());
            if (result != 0) return result;
        }
        return BY_NAME.compare(a, b);
    };

    public static boolean matches(InventoryItem item, StockFilter filter) {
        switch (filter) {
            case NEEDS_RESTOCK: return item.needsRestock();
            case OUT: return item.getQuantity() == 0;
            case LOW: return item.getQuantity() > 0 && item.needsRestock();
            case GOOD: return item.getQuantity() > 0 && !item.needsRestock();
            default: return true;
        }
    }

    /** O(n + k log k) time and O(k) extra storage for k selected records. */
    public static List<InventoryItem> select(List<InventoryItem> source,
            StockFilter filter, SortOrder order) {
        List<InventoryItem> selected = new ArrayList<>();
        for (InventoryItem item : source) if (matches(item, filter)) selected.add(item);
        Comparator<InventoryItem> comparator = order == SortOrder.RESTOCK_PRIORITY
                ? BY_RESTOCK_PRIORITY : order == SortOrder.QUANTITY ? BY_QUANTITY : BY_NAME;
        Collections.sort(selected, comparator);
        return selected;
    }

    /** Comparison implementation: drain the heap; iteration alone is not ordered.
     * Repeated offer/poll costs O(n + k log k), with O(k) storage.
     */
    public static List<InventoryItem> restockUsingHeap(List<InventoryItem> source) {
        PriorityQueue<InventoryItem> queue = new PriorityQueue<>(11, BY_RESTOCK_PRIORITY);
        for (InventoryItem item : source) if (item.needsRestock()) queue.offer(item);
        List<InventoryItem> result = new ArrayList<>(queue.size());
        while (!queue.isEmpty()) result.add(queue.poll());
        return result;
    }
}
