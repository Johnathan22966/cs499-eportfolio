import com.example.nexventory.CsvInventory;
import java.io.*;
import java.util.*;

public class CsvChecks {
    static int assertions;
    static void check(boolean yes) { assertions++; if(!yes)throw new AssertionError(); }
    static List<CsvInventory.Row> parse(String s)throws Exception{return CsvInventory.parse(new StringReader(s));}
    static void reject(String s,String message)throws Exception {
        try{parse(s);throw new AssertionError("Accepted invalid CSV");}
        catch(IllegalArgumentException e){check(e.getMessage().contains(message));}
    }
    public static void main(String[] args)throws Exception {
        String h=CsvInventory.HEADER+"\r\n";
        List<CsvInventory.Row> r=parse("\uFEFF"+h+"\"Cable, blue\",C1,0,5,Cables,Rack A\r\n\"Switch \"\"Core\"\"\",S1,2147483647,0,,\r\n");
        check(r.size()==2);check(r.get(0).name.equals("Cable, blue"));check(r.get(0).line==2);
        check(r.get(1).name.equals("Switch \"Core\""));check(r.get(1).quantity==Integer.MAX_VALUE);check(r.get(1).location.isEmpty());
        r=parse("name,sku,quantity,minimum_stock\nCable,C2,1,2");check(r.size()==1);check(r.get(0).category.isEmpty());
        r=parse(h+"\"Patch\ncable\",C3,1,2,,\n");check(r.get(0).name.equals("Patch\ncable"));
        reject(h+"Cable,C1,-1,2,,","Quantity");reject(h+"Cable,C1,1,2147483648,,","too large");
        reject(h+"Cable,C1,1.5,2,,","whole number");reject(h+"Cable, ,1,2,,","sku is required");
        reject(h+"Cable,C1,1,2,,\nOther,C1,2,3,,","duplicate sku");
        reject(h+"Cable,C1,1,2,","column count");reject("bad,header\n","header must");
        reject(h+"\"Cable,C1,1,2,,","unclosed");reject(h+"Ca\"ble,C1,1,2,,","quote must");
        reject(h+"\"Cable\"x,C1,1,2,,","after closing");reject(h+"Cable,C1,1,2,,,extra","too many");
        reject(h,"no inventory");reject("","missing header");reject(h+"Cable,C1,1,2,,\n\n","column count");
        reject(h+"X".repeat(513)+",C1,1,2,,","512");reject("X".repeat(CsvInventory.MAX_CHARS+1),"characters");
        StringBuilder batch=new StringBuilder(h);
        for(int i=0;i<1000;i++)batch.append("Cable,C").append(i).append(",1,2,,\n");
        check(parse(batch.toString()).size()==1000);
        batch.append("Cable,C1000,1,2,,\n");reject(batch.toString(),"1,000");
        // Quoted round trips across commas, quotes, CRLF and non-ASCII names.
        Random random=new Random(499);
        String chars="abc,\"\n é";
        for(int i=0;i<200;i++) {
            StringBuilder name=new StringBuilder("Cable");
            for(int j=0;j<20;j++)name.append(chars.charAt(random.nextInt(chars.length())));
            String value=name.toString().trim();
            r=parse(h+"\""+value.replace("\"","\"\"")+"\",SKU"+i+",3,9,,\n");
            check(r.get(0).name.equals(value));check(r.get(0).minimumStock==9);
        }
        System.out.println("CSV checks passed: "+assertions+" assertions");
    }
}
