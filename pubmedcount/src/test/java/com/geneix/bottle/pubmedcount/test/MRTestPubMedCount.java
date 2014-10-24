package com.geneix.bottle.pubmedcount.test;

import com.geneix.bottle.MedlineField;
import com.geneix.bottle.MedlineFieldDefinition;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by Andrew on 23/10/2014.
 */
public class MRTestPubMedCount {
    private static final Logger LOG = Logger.getLogger(MRTestPubMedCount.class);

    MapDriver<LongWritable, Text, Text, MedlineField> mapMedlineToFieldsDriver;
    MapDriver<Text, MedlineField, Text, WordHistogram> getFieldToHistogramDriver(){
        return MapDriver.newMapDriver(new PubMedCount.FieldToHistogram());
    }
    ReduceDriver<Text, WordHistogram, Text, WordHistogram> reduceDriver;
    MapReduceDriver<LongWritable, Text, Text, IntWritable, Text, IntWritable> mapReduceDriver;

    @Before
    public void setUp() {
        MapMedlineToFields medlineToFields = new MapMedlineToFields();
        PubMedCount.WordHistogramCombiner reducer = new PubMedCount.WordHistogramCombiner();
        mapMedlineToFieldsDriver = MapDriver.newMapDriver(medlineToFields);
        reduceDriver = ReduceDriver.newReduceDriver(reducer);
    }

    public static Pair<Text, MedlineField> firstFieldIntermediateResult = new Pair<>(new Text("PMID"), MedlineField.builder("PMID").addProperty("PMID", "25311385").build());
    public static Pair<Text, MedlineField> secondFieldIntermediateResult = new Pair<>(new Text("OWN"), MedlineField.builder("OWN").addProperty("OWN", "NLM").build());
    public static Pair<Text, MedlineField> abstractFieldIntermediateResult = new Pair<>(new Text("AB"), MedlineField.builder("AB").setType(MedlineFieldDefinition.FieldType.WORDS).addProperty("AB",
            "Opioid-related respiratory depression (RD) is a serious clinical problem as it" +
                    " causes multiple deaths and anoxic brain injuries. Morphine is subject to efflux" +
                    " via P-glycoprotein transporter encoded by ABCB1, also known as MDR1. ABCB1" +
                    " polymorphisms may affect blood-brain barrier transport of morphine and therefore" +
                    " individual response to its central analgesic and adverse effects. This study" +
                    " aimed to determine specific associations between common ABCB1 genetic variants" +
                    " and clinically important outcomes including RD and RD resulting in prolonged stay" +
                    " in hospital with intravenous morphine in a homogenous pediatric surgical pain" +
                    " population of 263 children undergoing tonsillectomy. Children with GG and GA" +
                    " genotypes of ABCB1 polymorphism rs9282564 had higher risks of RD resulting in" +
                    " prolonged hospital stays; adding one copy of the minor allele (G) increased the" +
                    " odds of prolonged hospital stay due to postoperative RD by 4.7-fold (95%" +
                    " confidence interval: 2.1-10.8, P=0.0002).The Pharmacogenomics Journal advance" +
                    " online publication, 14 October 2014; doi:10.1038/tpj.2014.56.").build());
    
    @Test
    public void testMedlineMapperBasics() throws IOException {
        Text firstRealEntry = new Text(MedlineExampleStrings.EXAMPLE_TEXT.split(PubMedCount.getDelimiter())[1]);
        mapMedlineToFieldsDriver.withInput(new LongWritable(), firstRealEntry);
        mapMedlineToFieldsDriver.withOutput(firstFieldIntermediateResult);
        mapMedlineToFieldsDriver.withOutput(secondFieldIntermediateResult);
        mapMedlineToFieldsDriver.runTest(false);
    }

    @Test
    public void testHistogramMapperBasics() throws IOException {

        getFieldToHistogramDriver()
                .withInput(firstFieldIntermediateResult)
                .withOutput(new Pair<>(new Text("PMID"), new WordHistogram().withValue("25311385")))
                .runTest(false);

        getFieldToHistogramDriver()
                .withInput(new Pair<>(new Text("Single value"), MedlineField.builder("Single value").addProperty("Single value", "These words should be treated as a single entity").setType(MedlineFieldDefinition.FieldType.SINGLE_TEXT_VALUE).build()))
                .withOutput(new Pair<>(new Text("Single value"), new WordHistogram().withValue("These words should be treated as a single entity")))
                .runTest(false);

    }

    public void testAbstractWordHistogram() throws IOException {
        List<Pair<Text, WordHistogram>> result = getFieldToHistogramDriver()
                .withInput(abstractFieldIntermediateResult).run();
        logResults(result);
        Assert.assertEquals(1,result.size());
        Pair<Text,WordHistogram> pair = result.get(0);
        WordHistogram histogram = pair.getSecond();
        TreeMap<Object, Object> histogramEntries = histogram.getReportItems();

        //Check some edge cases

        //First word in entry
        assertThat(histogramEntries, hasEntry((Object)"Opioid-related",(Object)1L));

        //Last word in entry
        assertThat(histogramEntries, hasEntry((Object)"doi:10.1038/tpj.2014.56.",(Object)1L));

        //Something that occurs more than once
        assertThat(histogramEntries, hasEntry((Object)"ABCB1",(Object)4L));

        //Alphabetically first word
        assertThat(histogramEntries, hasEntry((Object)"14",(Object)1L));

        //Alphabetically last word
        assertThat(histogramEntries, hasEntry((Object)"with",(Object)2L));
    }

    @Test
    public void testMapperMultiLineField() throws IOException {
        String entry = "25311385\n" +
                "AB  - Foo\n" +
                "      bar\r\n" +
                "      wibble\n";
        Text firstRealEntry = new Text(entry);
        mapMedlineToFieldsDriver.withInput(new LongWritable(), firstRealEntry);
        mapMedlineToFieldsDriver.withOutput(firstFieldIntermediateResult);
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
        assertThat(results, hasItem(firstFieldIntermediateResult));

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
