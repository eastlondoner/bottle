package com.geneix.bottle;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.aggregate.ValueAggregatorBaseDescriptor;
import org.apache.hadoop.mapreduce.lib.aggregate.ValueAggregatorCombiner;
import org.apache.hadoop.mapreduce.lib.aggregate.ValueAggregatorReducer;
import org.apache.hadoop.mapreduce.lib.aggregate.ValueHistogram;
import org.apache.hadoop.mapreduce.lib.chain.ChainMapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Created by andrew on 20/10/14.
 */
public class PubMedCount {

    private static final Log LOG = LogFactory.getLog(PubMedCount.class);
    private final static byte[] PUBMED_DELIMITER = Charsets.UTF_8.encode("PMID- ").array();

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "pubmedcount");

        job.setJarByClass(PubMedCount.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(ValueHistogram.class);

        ChainMapper.addMapper(job, MapMedlineToFields.class, LongWritable.class, Text.class, Text.class, Text.class, new Configuration(false));
        ChainMapper.addMapper(job, MapTextToWordCountHistogram.class, Text.class, Text.class, Text.class, WordHistogram.class, new Configuration(false));
        //ChainMapper.addMapper(job, MapWordCountHistogramToOut.class, Text.class, WordHistogram.class, Text.class, Text.class, new Configuration(false));

        //job.setMapperClass(Map.class);
        //job.setCombinerClass(WordHistogramCombiner.class);
        job.setReducerClass(WordHistogramCombiner.class);

        job.setInputFormatClass(PubMedFileInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2]));

        int code = job.waitForCompletion(true) ? 0 : 1;
        System.exit(code);
    }


    public static class PubMedFileInputFormat extends FileInputFormat<LongWritable, Text> {
        @Override
        public RecordReader<LongWritable, Text> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
            return new LineRecordReader(PUBMED_DELIMITER);
        }
    }

    public static class MapMedlineToFields extends Mapper<LongWritable, Text, Text, Text> {
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            //First check we haven't got some bogus whitespace or other junk
            String entry = value.toString().trim();
            if (entry.length() < 32) {
                LOG.info(String.format("Bogus line: %s", entry));
                return;
            }

            //Declare some variables
            Text outValue = new Text();
            Text outKey = new Text();

            //Tokenize the string into lines
            String[] strings = entry.split("\\r?\\n");

            //Handle the first token (because we split on PubMedId field identifier)
            String firstToken = strings[0].trim();

            outKey.set("PMID");
            StringBuilder sb = new StringBuilder();
            sb.append(firstToken);

            for (int i=1; i< strings.length; i++) {
                String line = strings[i];
                if (line.charAt(4) == '-') {
                    //first write the old field
                    LOG.info(line);
                    outValue.set(sb.toString());
                    sb = new StringBuilder();
                    if (outValue.getLength() > 0) {
                        LOG.info(String.format("MR1 KEY: %s; VALUE: %s", outKey, outValue));
                        context.write(outKey, outValue);
                        outValue.clear();
                    }

                    //Now start the new field
                    outKey.set(line.substring(0, 4).trim());

                }
                sb.append(line.substring(5));
            }
            //The final value is still in our 'buffer'
            outValue.set(sb.toString());
            LOG.info(String.format("MR1 KEY: %s; VALUE: %s", outKey, outValue));
            context.write(outKey, outValue);
            outValue.clear();
        }
    }

    public static class MapTextToWordCountHistogram extends Mapper<Text, Text, Text, WordHistogram> {
        public void map(Text key, Text value, Context context) throws IOException, InterruptedException {

            WordHistogram outValue = new WordHistogram();
            String[] words = value.toString().split("\\s+");
            for (String word : words) {
                //LOG.info(String.format("Histogramming KEY: %s; WORD: %s", key, word));
                outValue.addNextValue(word);
            }
            LOG.info(String.format("Histogram for KEY: %s; %s", key, outValue.getReport()));
            context.write(key, outValue);
        }
    }

    public static class MapWordCountHistogramToOut extends Mapper<Text, WordHistogram, Text, Text> {
        public static final Text EMPTY_TEXT = new Text();

        public static Text getKey(Text id) {
            return ValueAggregatorBaseDescriptor.generateEntry(ValueAggregatorBaseDescriptor.VALUE_HISTOGRAM, id.toString(), EMPTY_TEXT).getKey();
        }

        public void map(Text key, WordHistogram value, Context context
        ) throws IOException, InterruptedException {
            Text outKey = getKey(key);
            for (String s : value.getCombinerOutput()) {
                context.write(outKey, new Text(s));
            }
        }
    }
    public static class WordHistogramCombiner extends Reducer<Text, WordHistogram, Text, WordHistogram> {

        public void reduce(Text key, Iterable<WordHistogram> values, Context context
        ) throws IOException, InterruptedException {
            WordHistogram aggregator = new WordHistogram();
            int i=0;
            for (WordHistogram value : values) {

                //Holy cow, MapReduce just keeps reading into the exact same WordHistogram ... cheeky!

                i++;
                LOG.info(String.format("Histogram %s for KEY: %s; %s", i, key, value.getReport()));
                Iterator<?> datapoints = value.getCombinerOutput().iterator();
                while (datapoints.hasNext()) {
                    Object nextVal = datapoints.next();
                    LOG.info(String.format("REDUCER KEY: %s; VALUE: %s", key, nextVal));
                    aggregator.addNextValue(nextVal);
                }

            }
            context.write(key,aggregator);
        }
    }

}
