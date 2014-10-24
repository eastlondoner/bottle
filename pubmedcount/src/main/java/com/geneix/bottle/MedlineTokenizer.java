package com.geneix.bottle;

/**
 * Created by Andrew on 24/10/2014.
 */
public class MedlineTokenizer {
    private final String[] lines;
    private int position = 0;

    //PMID is always the first key
    private String nextKey = "PMID";

    public MedlineTokenizer(String[] lines){
        this.lines = lines;
    }

    public MedlineTokenizer(String medlineEntry){
        this(medlineEntry.split("\\r?\\n"));
    }

    public boolean hasNext(){
        return position < lines.length;
    }

    private String line(){
        return lines[position];
    }

    private String key() {
        //TODO: trim is probably an expensive way to do this
        return line().substring(0, 4).trim();
    }

    private String value(){
        if(position == 0){
            //Special case for the first line (PMID)
            return line();
        }
        return line().substring(6);
    }

    public MedlineField next() {
        StringBuilder sb = new StringBuilder();
        String primaryKey = nextKey;
        sb.append(value());

        MedlineFieldDefinition definition = MedlineFieldDefinitions.getDefinition(primaryKey);
        MedlineField.Builder fieldBuilder = MedlineField.builder(primaryKey);

        String currentKey = primaryKey;

        while (++position < lines.length){
            if (line().charAt(4) == '-') {
                nextKey = key();
                if(definition == null || !definition.matchesOtherFields(nextKey)){
                    //We are done
                    break;
                } else {
                    fieldBuilder.addProperty(currentKey, sb.toString());
                    currentKey = nextKey;
                    sb = new StringBuilder(value());
                }
            } else {
                sb.append(" ").append(value());
            }
        }
        fieldBuilder.addProperty(currentKey, sb.toString());

        return fieldBuilder.build();
    }


}
