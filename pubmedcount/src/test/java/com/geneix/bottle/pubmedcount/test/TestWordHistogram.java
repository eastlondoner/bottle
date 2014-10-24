package com.geneix.bottle.pubmedcount.test;

import com.geneix.bottle.WordHistogram;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Andrew on 24/10/2014.
 */
public class TestWordHistogram {
    @Test
    public void equality(){
        //Basics
        WordHistogram histogram1 = new WordHistogram().withValue("A").withValue("B\t2");
        WordHistogram histogram2 = new WordHistogram().withValue("A").withValue("B\t2");

        Assert.assertEquals(histogram1, histogram2);

        //Different order of building, should be the same tho
        WordHistogram histogram3 = new WordHistogram().withValue("B\t2").withValue("A");
        Assert.assertEquals(histogram1, histogram3);
        WordHistogram histogram4 = new WordHistogram().withValue("B").withValue("A").withValue("B");
        Assert.assertEquals(histogram1, histogram4);

        //Quick check of inequality
        WordHistogram histogram5 = new WordHistogram().withValue("A");
        Assert.assertNotEquals(histogram1, histogram5);
    }
}
