package com.geneix.bottle.mappers;

import com.geneix.bottle.MedlineField;
import com.geneix.bottle.MedlineTokenizer;
import com.geneix.bottle.PubMedCount;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.regex.Pattern;

/**
* Created by Andrew on 24/10/2014.
*/
public class MapMedlineToFields extends Mapper<LongWritable, Text, Text, MedlineField> {
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

        //First check we haven't got some bogus whitespace or other junk
        String entry = value.toString();
        if (!isValidEntry(entry)) {
            return;
        }

        MedlineTokenizer tokenizer = new MedlineTokenizer(entry);

        //We're going to do PMID first because we want to use it in keys
        MedlineField pmidField = tokenizer.next();
        Long PMID = Long.parseLong(pmidField.getValuesAsString());
        Text outKey = new Text("PMID");
        context.write(outKey, pmidField);

        while (tokenizer.hasNext()) {
            MedlineField field = tokenizer.next();
            outKey.set(field.name());
            context.write(outKey, field);
        }
    }

    private boolean isValidEntry(String entry) throws IOException {
        String shouldBePMID = entry.substring(0, entry.indexOf("\n")).trim();

        boolean isValid = numbers.matcher(shouldBePMID).matches();
        if (isValid) return true;

        String withoutWhitespace = entry.replaceAll("\\s|\\r|\\n|\\t", "");
        if (withoutWhitespace.isEmpty()) {
            if (PubMedCount.LOG.isInfoEnabled()) {
                PubMedCount.LOG.info(String.format("Bogus whitespace"));
            }
        } else {
            PubMedCount.LOG.warn(String.format("Bogus line: %s", entry));
            throw new IOException("Error parsing medline entry - UNEXPECTED DATA");
        }
        return false;
    }

    private static Pattern numbers = Pattern.compile("^[0-9][0-9]*$");
}
