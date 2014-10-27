package com.geneix.bottle.pubmedcount.test;

import com.geneix.bottle.MedlineGenerator;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by andrew on 27/10/14.
 */
public class TestMedlineGenerator {
    private final static Logger LOG = Logger.getLogger(TestMedlineGenerator.class);

    @Test
    public void basicGenerator() throws IOException {
        //Just check that nothing goes wrong using simple seed and running generate
        MedlineGenerator generator = new MedlineGenerator(Resources.toString(Resources.getResource("exampleSeed.txt"), Charsets.UTF_8));

        String generatedEntry = generator.generateEntry();

        LOG.info(generatedEntry);

        assertThat(generatedEntry, stringContainsInOrder(Arrays.asList("PMID- ","AB  - ")));

    }

}
