package com.geneix.bottle;

import com.google.common.base.Charsets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.mapreduce.lib.aggregate.ValueHistogram;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by andrew on 21/10/14.
 */
public class WordHistogram extends ValueHistogram implements Writable {

    private static final Log LOG = LogFactory.getLog(WordHistogram.class);

    public WordHistogram(){
        super();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        ArrayList<String> outData = getCombinerOutput();
        LOG.info(String.format("Writing output data. STRINGS: %s. HISTOGRAM:", outData.size(), getReportDetails()));
        //WritableUtils.writeVInt(out, 1);
        WritableUtils.writeStringArray(out, outData.toArray(new String[outData.size()]));
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        int treeSize = getReportItems().size();
        LOG.info(String.format("TREE SIZE ON READ: %s", treeSize));
        if(treeSize > 0){
            //Holy cow, MapReduce just keeps reading into the exact same instance in the reducer ... cheeky!
            //TODO: actually make use of this behaviour
            reset();
        }
        int len = in.readInt();
        LOG.info(String.format("Reading histogram data. STRINGS: %s", len));
        if (len == -1) return;
        for(int i = 0; i < len; i++) {
            String str = WritableUtils.readString(in);
            LOG.info(String.format("HISTOGRAM DATA: %s", str));
            addNextValue(str);
        }
    }

    @Override
    public synchronized void addNextValue(Object val){
        super.addNextValue(val);
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<Object,Object>> iter = getReportItems().entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<Object,Object> en =  iter.next();
            Object val = en.getKey();
            Long count = (Long) en.getValue();
            sb.append(val).append("\t").append(count.longValue()).append("; ");
        }
        return sb.append("\n").toString();
    }
}
