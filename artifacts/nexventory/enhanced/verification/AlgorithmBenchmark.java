package com.example.nexventory;

import java.util.*;

/** Warmed median elapsed times, not an Android UI or storage benchmark. */
public final class AlgorithmBenchmark {
    private static volatile long sink;
    private static List<InventoryItem> sorted(List<InventoryItem> data) {
        return InventoryAlgorithms.select(data,InventoryAlgorithms.StockFilter.NEEDS_RESTOCK,
                InventoryAlgorithms.SortOrder.RESTOCK_PRIORITY);
    }
    private static long time(List<InventoryItem> data, boolean heap) {
        long start=System.nanoTime();
        List<InventoryItem> result=heap ? InventoryAlgorithms.restockUsingHeap(data) : sorted(data);
        long elapsed=System.nanoTime()-start;
        sink += result.size() + (result.isEmpty()?0:result.get(0).getId());
        return elapsed;
    }
    public static void main(String[] args) {
        System.out.println("Java: " + System.getProperty("java.version") + "; VM: " + System.getProperty("java.vm.name"));
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch")
                + "; logical processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Seed 499; quantity/minimum uniform [0,99]; shuffled input; 12 warmups; median of 21 alternating trials.");
        System.out.println("n,k,sorted_list_median_ms,heap_drain_median_ms");
        for(int size:new int[]{100,1000,10000,100000}) {
            Random random=new Random(499);
            List<InventoryItem> data=new ArrayList<>();
            for(int i=0;i<size;i++) data.add(new InventoryItem(i,"Part " + i,"SKU-"+i,random.nextInt(100),random.nextInt(100)));
            Collections.shuffle(data,random);
            for(int i=0;i<12;i++) {time(data,false);time(data,true);}
            long[] a=new long[21], b=new long[21];
            for(int i=0;i<21;i++) {
                if(i%2==0) {a[i]=time(data,false);b[i]=time(data,true);}
                else {b[i]=time(data,true);a[i]=time(data,false);}
            }
            Arrays.sort(a);Arrays.sort(b);
            System.out.printf(Locale.ROOT,"%d,%d,%.4f,%.4f%n",size,sorted(data).size(),a[10]/1e6,b[10]/1e6);
        }
    }
}
