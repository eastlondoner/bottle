package com.geneix.bottle;

import com.geneix.bottle.mappers.MapMedlineToFields;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableCollection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.aggregate.ValueAggregatorBaseDescriptor;
import org.apache.hadoop.mapreduce.lib.aggregate.ValueHistogram;
import org.apache.hadoop.mapreduce.lib.chain.ChainMapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by andrew on 20/10/14.
 */
public class PubMedCount {

    private static final Log LOG = LogFactory.getLog(PubMedCount.class);
    private final static byte[] PUBMED_DELIMITER = Charsets.UTF_8.encode("PMID- ").array();

    public static String getDelimiter() {
        return new String(PUBMED_DELIMITER, Charsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "pubmedcount");

        job.setJarByClass(PubMedCount.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(ValueHistogram.class);

        addMappers(job);

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

    private static void addMappers(Job job) throws IOException {
        ChainMapper.addMapper(job, MapMedlineToFields.class, LongWritable.class, Text.class, Text.class, MedlineField.class, new Configuration(false));
        ChainMapper.addMapper(job, FieldToHistogram.class, Text.class, MedlineField.class, Text.class, WordHistogram.class, new Configuration(false));
        //ChainMapper.addMapper(job, MapWordCountHistogramToOut.class, Text.class, WordHistogram.class, Text.class, Text.class, new Configuration(false));
    }


    public static class PubMedFileInputFormat extends FileInputFormat<LongWritable, Text> {
        @Override
        public RecordReader<LongWritable, Text> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
            return new LineRecordReader(PUBMED_DELIMITER);
        }
    }


    public static class FieldToHistogram extends Mapper<Text, MedlineField, Text, WordHistogram> {
        public void map(Text key, MedlineField value, Context context) throws IOException, InterruptedException {
            String[] words;
            switch (value.getType()) {
                case WORDS:
                    words = value.getValuesAsString().split("[-|\"|'|,|.|!|;|;|(|)|\\[|\\]]*\\s+[-|\"|'|,|.|!|;|;|(|)|\\[|\\]]*");
                    break;
                case SINGLE_TEXT_VALUE:
                    words = new String[]{value.getValuesAsString()};
                    break;
                case SINGLE_OBJECT_VALUE:
                    ImmutableCollection<Map.Entry<String, String>> entries = value.getEntries();
                    words = new String[entries.size()];
                    int i = 0;
                    for (Map.Entry<String, String> entry : entries) {
                        words[i++] = String.format("%s:%s", entry.getKey(), entry.getValue());
                    }
                    break;
                case ARRAY_TEXT_VALUES:
                    entries = value.getEntries();
                    words = new String[entries.size()];
                    int j = 0;
                    for (Map.Entry<String, String> entry : entries) {
                        words[j++] = entry.getValue();
                    }
                    break;
                default:
                    LOG.error("Unexpected type " + value.getType().name());
                    words = null; // This will cause an NPE below. This should never happen
            }

            WordHistogram outValue = new WordHistogram();
            for (String word : words) {
                //LOG.info(String.format("Histogramming KEY: %s; WORD: %s", key, word));
                word.replaceAll("\\t","");
                outValue.addNextValue(word);
            }
            if(LOG.isInfoEnabled()) {
                LOG.info(String.format("Histogram for KEY: %s; %s", key, outValue.getReport()));
            }
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
            int i = 0;
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
            context.write(key, aggregator);
        }
    }

}
