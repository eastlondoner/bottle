package com.geneix.bottle;

import java.io.IOException;
import java.nio.charset.CharsetEncoder;

import com.google.common.base.Charsets;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

/**
 * Created by andrew on 20/10/14.
 */
public class PubMedCount {


    private final static byte[] PUBMED_DELIMITER = Charsets.UTF_8.encode("PMID-").array();

    //Need a tokenizer
    public static class PubMedFileInputFormat extends FileInputFormat<LongWritable,Text>{
        @Override
        public RecordReader<LongWritable, Text> createRecordReader(
                InputSplit split, TaskAttemptContext context) throws IOException,
                InterruptedException {
            return new LineRecordReader(PUBMED_DELIMITER);
        }
    }
}
