package com.example.nexventory;
import java.io.StringReader;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class CsvInventoryTest {
    private List<CsvInventory.Row> parse(String s) throws Exception {return CsvInventory.parse(new StringReader(s));}
    @Test public void readsQuotedFieldsAndOptionalMetadata() throws Exception {
        List<CsvInventory.Row> r=parse(CsvInventory.HEADER+"\r\n\"Cable, blue\",C1,0,5,Cables,Rack A\r\n\"Switch \"\"Core\"\"\",S1,1,2,,\r\n");
        assertEquals(2,r.size());assertEquals("Cable, blue",r.get(0).name);
        assertEquals("Rack A",r.get(0).location);assertEquals("Switch \"Core\"",r.get(1).name);
        assertEquals("",r.get(1).category);
    }
    @Test public void basicHeaderAndMultilineNameAreSupported() throws Exception {
        List<CsvInventory.Row> r=parse("\uFEFFname,sku,quantity,minimum_stock\n\"Patch\ncable\",C1,3,9\n");
        assertEquals("Patch\ncable",r.get(0).name);assertEquals("",r.get(0).location);
    }
    @Test public void rejectsInvalidStockDuplicatesAndMalformedQuotes() throws Exception {
        for(String row:new String[]{"Cable,C1,-1,5,,","Cable,C1,1,2147483648,,","Cable,C1,1.5,5,,",
                "Cable, ,1,5,,","\"Cable\"x,C1,1,5,,","\"Cable,C1,1,5,,",
                "Cable,C1,1,5,,\nOther,C1,1,5,,","Cable,C1,1,5,"}) {
            try{parse(CsvInventory.HEADER+"\n"+row);fail("Accepted invalid CSV");}
            catch(IllegalArgumentException e){assertTrue(e.getMessage().contains("Nothing was imported"));}
        }
    }
    @Test public void rejectsOversizedFieldsFilesAndBatches() throws Exception {
        StringBuilder s=new StringBuilder(CsvInventory.HEADER+"\n");
        for(int i=0;i<1001;i++)s.append("Cable,C").append(i).append(",1,5,,\n");
        for(String value:new String[]{s.toString(),"X".repeat(CsvInventory.MAX_CHARS+1),
                CsvInventory.HEADER+"\n"+"X".repeat(513)+",C1,1,5,,"}) {
            try{parse(value);fail("Accepted oversized CSV");}catch(IllegalArgumentException expected){}
        }
    }
}
