package com.example.nexventory;
import java.io.*;
import java.util.*;
/** Bounded CSV parser: quoted commas, escaped quotes and multiline fields. */
public final class CsvInventory {
    public static final int MAX_CHARS=1048576, MAX_ROWS=1000, MAX_FIELD=512;
    public static final String HEADER="name,sku,quantity,minimum_stock,category,location";
    private CsvInventory() {}
    public static final class Row {
        public final int line,quantity,minimumStock;
        public final String name,sku,category,location;
        Row(int line,List<String> f) {
            this.line=line;
            try {
                name=InputRules.required(f.get(0),"name"); sku=InputRules.required(f.get(1),"sku");
                quantity=InputRules.quantity(f.get(2).trim()); minimumStock=InputRules.minimumStock(f.get(3).trim());
                category=f.size()==6?f.get(4).trim():""; location=f.size()==6?f.get(5).trim():"";
            } catch(IllegalArgumentException e) {throw fail(line,e.getMessage());}
        }
    }
    private static IllegalArgumentException fail(int line,String message) {
        return new IllegalArgumentException("CSV line "+line+": "+message+". Nothing was imported.");
    }
    public static List<Row> parse(Reader reader) throws IOException {
        StringBuilder text=new StringBuilder(); char[] buffer=new char[4096]; int n;
        while((n=reader.read(buffer))!=-1) {
            if(text.length()+n>MAX_CHARS) throw fail(1,"file exceeds 1,048,576 characters");
            text.append(buffer,0,n);
        }
        if(text.length()>0&&text.charAt(0)=='\uFEFF') text.deleteCharAt(0);
        List<List<String>> records=new ArrayList<>(); List<Integer> lines=new ArrayList<>();
        List<String> fields=new ArrayList<>(); StringBuilder field=new StringBuilder();
        boolean quoted=false,closed=false; int line=1,start=1;
        for(int i=0;i<text.length();i++) {
            char ch=text.charAt(i);
            if(quoted) {
                if(ch=='"') {
                    if(i+1<text.length()&&text.charAt(i+1)=='"') {field.append('"');i++;}
                    else {quoted=false;closed=true;}
                } else {
                    field.append(ch);
                    if(ch=='\n'||(ch=='\r'&&(i+1==text.length()||text.charAt(i+1)!='\n'))) line++;
                }
            } else if(ch==','||ch=='\r'||ch=='\n') {
                fields.add(field.toString());field.setLength(0);closed=false;
                if(fields.size()>6) throw fail(start,"too many columns");
                if(ch!=',') {
                    if(ch=='\r'&&i+1<text.length()&&text.charAt(i+1)=='\n')i++;
                    records.add(fields);lines.add(start);fields=new ArrayList<>();
                    if(records.size()>MAX_ROWS+1)throw fail(start,"maximum is 1,000 inventory rows");
                    line++;start=line;
                }
            } else if(closed) throw fail(line,"unexpected character after closing quote");
            else if(ch=='"') {
                if(field.length()!=0)throw fail(line,"quote must begin a field");
                quoted=true;
            } else field.append(ch);
            if(field.length()>MAX_FIELD)throw fail(line,"field exceeds 512 characters");
        }
        if(quoted)throw fail(start,"unclosed quoted field");
        if(field.length()>0||!fields.isEmpty()||closed) {
            fields.add(field.toString());
            if(fields.size()>6)throw fail(start,"too many columns");
            records.add(fields);lines.add(start);
        }
        if(records.isEmpty())throw fail(1,"missing header");
        List<String> header=records.get(0);
        if(!header.equals(Arrays.asList(HEADER.split(",")))&&!header.equals(Arrays.asList("name","sku","quantity","minimum_stock")))
            throw fail(1,"header must be "+HEADER+" (last two columns may be omitted)");
        if(records.size()==1)throw fail(1,"file has no inventory rows");
        if(records.size()>MAX_ROWS+1)throw fail(1,"maximum is 1,000 inventory rows");
        List<Row> rows=new ArrayList<>();Set<String> skus=new HashSet<>();
        for(int i=1;i<records.size();i++) {
            if(records.get(i).size()!=header.size())throw fail(lines.get(i),"column count does not match header");
            Row row=new Row(lines.get(i),records.get(i));
            if(!skus.add(row.sku))throw fail(row.line,"duplicate sku "+row.sku);
            rows.add(row);
        }
        return rows;
    }
}
