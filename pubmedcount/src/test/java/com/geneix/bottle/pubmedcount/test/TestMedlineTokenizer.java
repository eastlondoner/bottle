package com.geneix.bottle.pubmedcount.test;

import com.geneix.bottle.MedlineField;
import com.geneix.bottle.MedlineTokenizer;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Andrew on 24/10/2014.
 */
public class TestMedlineTokenizer {
    Logger LOG = Logger.getLogger(TestMedlineTokenizer.class);

    @Test
    /**
     * This test doesn't break out the functionality of the tokenizer specifically but it does a useful set of
     * checks on an actual example of a pubmed entry.
     *
     */
    public void testExample() {
        MedlineTokenizer tokenizer = new MedlineTokenizer(MedlineExampleStrings.getExamplesAsArray()[1]);
        int totalCount = 0;
        int fauCount = 0;
        MedlineField field = null;
        while (tokenizer.hasNext()) {
            totalCount++;
            field = tokenizer.next();
            if (totalCount == 1) {
                Assert.assertEquals("First field name", "PMID", field.name());
                Assert.assertEquals("Check the first field", MedlineField.builder("PMID").addProperty("PMID", "25311385").build(), field);
            }
            if (field.name().equals("FAU")) {
                fauCount++;
                if (fauCount == 1) {
                    Assert.assertEquals(
                            "Check first FAU field (multiple properties)",
                            MedlineField.builder("FAU")
                                    .addProperty("FAU", "Sadhasivam, S")
                                    .addProperty("AU", "Sadhasivam S")
                                    .addProperty("AD", "1] Department of Anesthesia, Cincinnati Children's Hospital Medical Center, Cincinnati, OH, USA [2] Department of Pediatrics, Cincinnati Children's Hospital Medical Center, Cincinnati, OH, USA.")
                                    .build(),
                            field
                    );
                }
            }
        }
        LOG.info(field.toString());

        Assert.assertEquals("Check the last field name", field.name(), "SO");
        Assert.assertEquals("Check the last field", field, MedlineField.builder("SO").

                addProperty("SO", "Pharmacogenomics J. 2014 Oct 14. doi: 10.1038/tpj.2014.56.").build());
        Assert.assertEquals("Check total number of fields", 31, totalCount);
        Assert.assertEquals("Check number of FAU fields", 8, fauCount);
    }
}
