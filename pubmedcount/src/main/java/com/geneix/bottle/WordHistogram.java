package com.geneix.bottle;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.mapreduce.lib.aggregate.ValueHistogram;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by andrew on 21/10/14.
 */
public class WordHistogram extends ValueHistogram implements Writable {

    @Override
    public void write(DataOutput out) throws IOException {
        ArrayList<String> outdata = getCombinerOutput();
        WritableUtils.writeStringArray(out, outdata.toArray(new String[outdata.size()]));
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        String[] data = WritableUtils.readStringArray(in);
        for (String aData : data) {
            addNextValue(aData);
        }
    }
}
