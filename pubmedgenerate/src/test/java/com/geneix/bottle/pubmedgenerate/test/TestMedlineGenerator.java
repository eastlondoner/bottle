package com.geneix.bottle.pubmedgenerate.test;

import com.geneix.bottle.MedlineGenerator;
import com.geneix.bottle.MedlineTokenizer;
import com.geneix.bottle.PubMedCount;
import com.geneix.bottle.mappers.MapMedlineToFields;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.stringContainsInOrder;

/**
 * Created by andrew on 27/10/14.
 */
public class TestMedlineGenerator {
    private final static Logger LOG = Logger.getLogger(TestMedlineGenerator.class);

    private static MedlineGenerator getMedlineGenerator() throws IOException {
        return new MedlineGenerator(Resources.toString(Resources.getResource("exampleSeed.txt"), Charsets.UTF_8));
    }

    @Test
    public void basicGenerator() throws IOException {
        //Just check that nothing goes wrong using simple seed and running generate
        MedlineGenerator generator = getMedlineGenerator();

        String generatedEntry = generator.generateEntry();

        LOG.info(generatedEntry);

        assertThat(generatedEntry, stringContainsInOrder(Arrays.asList("PMID- ", "AB  - ")));
    }

    @Test
    public void checkGeneratorEntryParses() throws IOException {
        MedlineGenerator generator = getMedlineGenerator();
        MedlineTokenizer tokenizer = new MedlineTokenizer(generator.generateEntry());
        parseEntryWithAssertions(tokenizer, true);
    }

    private void parseEntryWithAssertions(MedlineTokenizer tokenizer, boolean log) {
        int i = 0;
        while (tokenizer.hasNext()){
            String fieldString = tokenizer.next().toString();
            if(log) {
                LOG.info(fieldString);
            }
            assertThat(fieldString.length(), greaterThan(6));
            i++;
        }
        Assert.assertEquals(39,i);
    }

    @Test
    public void checkGeneratorOutputParses() throws IOException {
        MedlineGenerator generator = getMedlineGenerator();
        File file = new File("testfile.txt");
        OutputStream out = new FileOutputStream(file);

        int generatedCount = 1000;

        try {
            generator.generateEntries(out, generatedCount);
        } finally {
            out.close();
        }

        Scanner in = new Scanner(file).useDelimiter(PubMedCount.getDelimiter());
        int inCount = 0;
        try{
            while (in.hasNext()){
                String entry = in.next();
                if(MapMedlineToFields.isValidEntry(entry)){
                    MedlineTokenizer tokenizer = new MedlineTokenizer(entry);
                    parseEntryWithAssertions(tokenizer, false);
                    inCount++;
                }
            }
        } finally {
            in.close();
        }
        Assert.assertEquals(generatedCount, inCount);
    }
}
