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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by Andrew on 23/10/2014.
 */
public class MRTestPubMedCount {
    private static final Logger LOG = Logger.getLogger(MRTestPubMedCount.class);
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
    MapDriver<LongWritable, Text, Text, MedlineField> mapMedlineToFieldsDriver;

    MapReduceDriver<LongWritable, Text, Text, IntWritable, Text, IntWritable> mapReduceDriver;

    private static <X, Y> void logResults(List<Pair<X, Y>> results) {
        for (Pair<?, ?> result : results) {
            LOG.info(String.format("KEY: %s; VALUE:%s;", result.getFirst(), result.getSecond()));
        }
    }

    private static ReduceDriver<Text, WordHistogram, Text, WordHistogram> getReduceDriver(){
        return  ReduceDriver.newReduceDriver(new PubMedCount.WordHistogramCombiner());
    }

    private static MapDriver<Text, MedlineField, Text, WordHistogram> getFieldToHistogramDriver() {
        return MapDriver.newMapDriver(new PubMedCount.FieldToHistogram());
    }

    @Before
    public void setUp() {
        MapMedlineToFields medlineToFields = new MapMedlineToFields();
        mapMedlineToFieldsDriver = MapDriver.newMapDriver(medlineToFields);
    }

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

        //Simple case
        getFieldToHistogramDriver()
                .withInput(firstFieldIntermediateResult)
                .withOutput(new Pair<>(new Text("PMID"), new WordHistogram().withValue("25311385")))
                .runTest(false);

        //Single value field type
        getFieldToHistogramDriver()
                .withInput(new Pair<>(new Text("Single value"), MedlineField.builder("Single value").addProperty("Single value", "These words should be treated as a single entity").setType(MedlineFieldDefinition.FieldType.SINGLE_TEXT_VALUE).build()))
                .withOutput(new Pair<>(new Text("Single value"), new WordHistogram().withValue("These words should be treated as a single entity")))
                .runTest(false);

        //WORDS field type
        getFieldToHistogramDriver()
                .withInput(new Pair<>(new Text("Words"), MedlineField.builder("Words").addProperty("Words", "These words should be treated seperately").setType(MedlineFieldDefinition.FieldType.WORDS).build()))
                .withOutput(new Pair<>(new Text("Words"), new WordHistogram().withValue("These").withValue("words").withValue("should").withValue("be").withValue("treated").withValue("seperately")))
                .runTest(false);

        //Array field type
        getFieldToHistogramDriver()
                .withInput(new Pair<>(new Text("Words"), MedlineField.builder("Array")
                        .addProperty("Array", "These words should be treated as a single entity")
                        .addProperty("Array", "and these")
                        .addProperty("Array", "and these")
                        .addProperty("Array", "and finally these")
                        .addProperty("Other", "and even these, although it's not expected")
                        .setType(MedlineFieldDefinition.FieldType.ARRAY_TEXT_VALUES).build()))
                .withOutput(
                        new Pair<>(new Text("Words"),
                                new WordHistogram()
                                        .withValue("These words should be treated as a single entity")
                                        .withValue("and these\t2")
                                        .withValue("and finally these")
                                        .withValue("and even these, although it's not expected")
                        ))
                .runTest(false);


        //Object field type
        getFieldToHistogramDriver()
                .withInput(new Pair<>(new Text("Object"), MedlineField.builder("Object")
                        .addProperty("Foo", "These words should be treated as a single entity")
                        .addProperty("Bar", "and these")
                        .addProperty("Bar", "and these")
                        .addProperty("Foo", "and these")
                        .addProperty("Other", "and even these")
                        .setType(MedlineFieldDefinition.FieldType.SINGLE_OBJECT_VALUE).build()))
                .withOutput(
                        new Pair<>(new Text("Object"),
                                new WordHistogram()
                                        .withValue("Foo:These words should be treated as a single entity")
                                        .withValue("Bar:and these\t2")
                                        .withValue("Foo:and these")
                                        .withValue("Other:and even these")
                        ))
                .runTest(false);
    }

    @Test
    public void testAbstractWordHistogram() throws IOException {
        List<Pair<Text, WordHistogram>> result = getFieldToHistogramDriver()
                .withInput(abstractFieldIntermediateResult).run();
        logResults(result);
        Assert.assertEquals(1, result.size());
        Pair<Text, WordHistogram> pair = result.get(0);
        WordHistogram histogram = pair.getSecond();
        TreeMap<Object, Object> histogramEntries = histogram.getReportItems();

        //Check some edge cases

        //First word in entry
        assertThat(histogramEntries, hasEntry((Object) "Opioid-related", (Object) 1L));

        //Last word in entry
        assertThat(histogramEntries, hasEntry((Object) "doi:10.1038/tpj.2014.56.", (Object) 1L));

        //Something that occurs more than once
        assertThat(histogramEntries, hasEntry((Object) "ABCB1", (Object) 4L));

        //Alphabetically first word
        assertThat(histogramEntries, hasEntry((Object) "14", (Object) 1L));

        //Alphabetically last word
        assertThat(histogramEntries, hasEntry((Object) "with", (Object) 2L));
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
        mapMedlineToFieldsDriver.withOutput(new Text("AB"), MedlineField.builder("AB").addProperty("AB", "Foo bar wibble").build());
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
        assertThat(results, hasItem(new Pair<>(new Text("SO"), MedlineField.builder("SO").addProperty("SO", "Pharmacogenomics J. 2014 Oct 14. doi: 10.1038/tpj.2014.56.").build())));

        //Check that difficult item has correct format
        assertThat(results, hasItem(new Pair<>(new Text("FAU"), MedlineField.builder("FAU").addProperty("FAU", "Sadhasivam, S").addProperty("AU", "Sadhasivam S").addProperty("AD", "1] Department of Anesthesia, Cincinnati Children's Hospital Medical Center, Cincinnati, OH, USA [2] Department of Pediatrics, Cincinnati Children's Hospital Medical Center, Cincinnati, OH, USA.").build())));
    }

    @Test
    public void testReducer() throws IOException {
        //Simple additive tests
        List<WordHistogram> values = new ArrayList<>();
        values.add(new WordHistogram().withValue("A"));
        values.add(new WordHistogram().withValue("A"));
        getReduceDriver()
                .withInput(new Text("Combining Simple Histograms"), values)
                .withOutput(new Text("Combining Simple Histograms"), new WordHistogram().withValue("A\t2"))
                .runTest();

        values = new ArrayList<>();
        values.add(new WordHistogram().withValue("A"));
        values.add(new WordHistogram().withValue("A\t2"));
        getReduceDriver()
                .withInput(new Text("Combining Simple Histograms"), values)
                .withOutput(new Text("Combining Simple Histograms"), new WordHistogram().withValue("A\t3"))
                .runTest();

        values = new ArrayList<>();
        values.add(new WordHistogram().withValue("A"));
        values.add(new WordHistogram().withValue("B\t2"));
        getReduceDriver()
                .withInput(new Text("Combining Simple Histograms"), values)
                .withOutput(new Text("Combining Simple Histograms"), new WordHistogram().withValue("A").withValue("B\t2"))
                .runTest();

        values = new ArrayList<>();
        values.add(new WordHistogram().withValue("A"));
        values.add(new WordHistogram().withValue("A").withValue("B"));
        values.add(new WordHistogram().withValue("A").withValue("B").withValue("C"));
        getReduceDriver()
                .withInput(new Text("Combining Simple Histograms"), values)
                .withOutput(new Text("Combining Simple Histograms"), new WordHistogram().withValue("A\t3").withValue("B\t2").withValue("C\t1"))
                .runTest();


        //Quick test case sensitivity
        values = new ArrayList<>();
        values.add(new WordHistogram().withValue("A"));
        values.add(new WordHistogram().withValue("a").withValue("b"));
        values.add(new WordHistogram().withValue("A").withValue("B").withValue("C"));
        values.add(new WordHistogram().withValue("a").withValue("b").withValue("c").withValue("d"));
        getReduceDriver()
                .withInput(new Text("Combining Simple Histograms"), values)
                .withOutput(new Text("Combining Simple Histograms"), new WordHistogram().withValue("a\t2").withValue("A\t2").withValue("b\t2").withValue("B").withValue("c").withValue("C\t1").withValue("d"))
                .runTest();

    }

}
