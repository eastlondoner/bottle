package com.geneix.bottle.pubmedcount.test;

import com.geneix.bottle.MedlineField;
import com.geneix.bottle.PubMedCount;
import com.geneix.bottle.WordHistogram;
import com.geneix.bottle.mappers.MapMedlineToFields;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.apache.hadoop.mrunit.types.Pair;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew on 23/10/2014.
 */
public class MRTestPubMedCount {
    private static final Logger LOG = Logger.getLogger(MRTestPubMedCount.class);

    MapDriver<LongWritable, Text, Text, MedlineField> mapMedlineToFieldsDriver;
    MapDriver<Text, MedlineField, Text, WordHistogram> mapTextToHistogramDriver;
    ReduceDriver<Text, WordHistogram, Text, WordHistogram> reduceDriver;
    MapReduceDriver<LongWritable, Text, Text, IntWritable, Text, IntWritable> mapReduceDriver;


    @Before
    public void setUp() {
        MapMedlineToFields medlineToFields = new MapMedlineToFields();
        PubMedCount.FieldToHistogram textToHistogram = new PubMedCount.FieldToHistogram();
        PubMedCount.WordHistogramCombiner reducer = new PubMedCount.WordHistogramCombiner();
        mapMedlineToFieldsDriver = MapDriver.newMapDriver(medlineToFields);
        mapTextToHistogramDriver = MapDriver.newMapDriver(textToHistogram);
        reduceDriver = ReduceDriver.newReduceDriver(reducer);
    }

    @Test
    public void testMapperBasics() throws IOException {
        Text firstRealEntry = new Text(MedlineExampleStrings.EXAMPLE_TEXT.split(PubMedCount.getDelimiter())[1]);
        mapMedlineToFieldsDriver.withInput(new LongWritable(), firstRealEntry);
        mapMedlineToFieldsDriver.withOutput(new Text("PMID"), MedlineField.builder("PMID").addProperty("PMID", "25311385").build());
        mapMedlineToFieldsDriver.withOutput(new Text("OWN"), MedlineField.builder("OWN").addProperty("OWN", "NLM").build());
        mapMedlineToFieldsDriver.runTest(false);
    }

    @Test
    public void testMapperMultiLineField() throws IOException {
        String entry = "25311385\n" +
                "AB  - Foo\n" +
                "      bar\r\n" +
                "      wibble\n";
        Text firstRealEntry = new Text(entry);
        mapMedlineToFieldsDriver.withInput(new LongWritable(), firstRealEntry);
        mapMedlineToFieldsDriver.withOutput(new Text("PMID"),  MedlineField.builder("PMID").addProperty("PMID", "25311385").build());
        mapMedlineToFieldsDriver.withOutput(new Text("AB"),  MedlineField.builder("AB").addProperty("AB", "Foo bar wibble").build());
        mapMedlineToFieldsDriver.runTest(false);
    }

    @Test
    public void testMapperEmptyEntry() throws IOException {
        String entry = "\n" +
                "\r\n" +
                "      \n";
        Text firstRealEntry = new Text(entry);
        mapMedlineToFieldsDriver.withInput(new LongWritable(), firstRealEntry);
        List<Pair<Text, MedlineField>> results = mapMedlineToFieldsDriver.run();

        logResults(results);

        assertThat(results, hasSize(0));
    }

    @Test
    public void testMapperExample() throws IOException {
        Text firstRealEntry = new Text(MedlineExampleStrings.getExamplesAsArray()[1]);
        mapMedlineToFieldsDriver.withInput(new LongWritable(), firstRealEntry);
        List<Pair<Text, MedlineField>> results = mapMedlineToFieldsDriver.run();

        logResults(results);

        //First item in entry
        assertThat(results, hasItem(new Pair<>(new Text("PMID"), MedlineField.builder("PMID").addProperty("PMID", "25311385").build())));

        //Result set size
        assertThat(results, hasSize(31));

        //Last item in entry
        assertThat(results, hasItem(new Pair<>(new Text("SO"), MedlineField.builder("SO").addProperty("SO","Pharmacogenomics J. 2014 Oct 14. doi: 10.1038/tpj.2014.56.").build())));

        //Check that difficult item has correct format
        assertThat(results, hasItem(new Pair<>(new Text("FAU"), MedlineField.builder("FAU").addProperty("FAU","Sadhasivam, S").addProperty("AU","Sadhasivam S").addProperty("AD","1] Department of Anesthesia, Cincinnati Children's Hospital Medical Center, Cincinnati, OH, USA [2] Department of Pediatrics, Cincinnati Children's Hospital Medical Center, Cincinnati, OH, USA.").build())));
    }

    private static <X,Y> void logResults(List<Pair<X,Y>> results) {
        for (Pair<?,?> result : results) {
            LOG.info(String.format("KEY: %s; VALUE:%s;",result.getFirst(), result.getSecond()));
        }
    }


    @Test
    public void testReducer() {
        List<IntWritable> values = new ArrayList<IntWritable>();
        values.add(new IntWritable(1));
        values.add(new IntWritable(1));
        //reduceDriver.withInput(new Text("6"), values);
        //reduceDriver.withOutput(new Text("6"), new IntWritable(2));
        //reduceDriver.runTest();
    }

}
