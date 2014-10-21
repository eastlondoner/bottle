package com.geneix.bottle;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.aggregate.ValueAggregatorBaseDescriptor;
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

    private final static byte[] PUBMED_DELIMITER = Charsets.UTF_8.encode("\nPMID- ").array();

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "pubmedcount");

        job.setJarByClass(PubMedCount.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(ValueHistogram.class);

        ChainMapper.addMapper(job, MapMedlineToFields.class, LongWritable.class, Text.class, Text.class, Text.class, new Configuration(false));
        ChainMapper.addMapper(job, MapTextToWordCountHistogram.class, Text.class, Text.class, Text.class, WordHistogram.class, new Configuration(false));

        //job.setMapperClass(Map.class);
        job.setCombinerClass(WordHistogramCombiner.class);
        job.setReducerClass(ValueAggregatorReducer.class);

        job.setInputFormatClass(PubMedFileInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2]));

        int code = job.waitForCompletion(true) ? 0 : 1;
        System.exit(code);
    }

    public static class WordHistogramCombiner extends Reducer<Text, WordHistogram, Text, Text> {
        public static final Text EMPTY_TEXT = new Text();

        public static Text getKey(Text id) {
            return ValueAggregatorBaseDescriptor.generateEntry(ValueAggregatorBaseDescriptor.VALUE_HISTOGRAM, id.toString(), EMPTY_TEXT).getKey();
        }

        public void reduce(Text key, Iterable<WordHistogram> values, Context context
        ) throws IOException, InterruptedException {
            ValueHistogram aggregator = new ValueHistogram();
            for (WordHistogram value : values) {
                Iterator<?> datapoints = value.getCombinerOutput().iterator();
                while (datapoints.hasNext()) {
                    aggregator.addNextValue(datapoints.next());
                }
            }
            Text outKey = getKey(key);
            for (String s : aggregator.getCombinerOutput()) {
                context.write(outKey, new Text(s));
            }
        }
    }

    public static class PubMedFileInputFormat extends FileInputFormat<LongWritable, Text> {
        @Override
        public RecordReader<LongWritable, Text> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
            return new LineRecordReader(PUBMED_DELIMITER);
        }
    }

    public static class MapMedlineToFields extends Mapper<LongWritable, Text, Text, Text> {
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            //First check we haven't got some bogus whitespace
            String entry = value.toString().trim();
            if (Strings.isNullOrEmpty(entry)) {
                return;
            }

            //Declare some variables
            Text outValue = new Text();
            Text outKey = new Text();

            //Tokenize the string into lines
            StringTokenizer tokenizer = new StringTokenizer(entry, "\n");

            //Handle the first token (because we split on PubMedId field identifier)
            String firstToken = tokenizer.nextToken();
            outKey.set("PMID");
            outValue.set(firstToken);
            while (tokenizer.hasMoreTokens()) {
                String line = tokenizer.nextToken();
                if (line.charAt(4) == '-') {
                    //first write the old field
                    if (outValue.getLength() > 0) {
                        context.write(outKey, outValue);
                    }

                    //Now start the new field
                    outKey = new Text(line.substring(0, 4).trim());
                    outValue = new Text();
                }
                byte[] bytes = Charsets.UTF_8.encode(line.substring(5).trim()).array();
                outValue.append(bytes, 0, bytes.length);
            }
            //The final value is still in our 'buffer'
            context.write(outKey, outValue);
        }
    }

    public static class MapTextToWordCountHistogram extends Mapper<Text, Text, Text, WordHistogram> {
        public void map(Text key, Text value, Context context) throws IOException, InterruptedException {

            WordHistogram outValue = new WordHistogram();
            StringTokenizer tokenizer = new StringTokenizer(value.toString());
            while (tokenizer.hasMoreTokens()) {
                outValue.addNextValue(tokenizer.nextToken());
            }
            context.write(key, outValue);
        }
    }

}
