package com.geneix.bottle;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.math3.util.Pair;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;

/**
 * Created by andrew on 29/10/14.
 */
public class GeneratePubMedData {

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "generatepubmeddata");

        job.setJarByClass(GeneratePubMedData.class);

        DataGenerator generator = new DataGenerator(new PubMedGeneratorInputFormat());
        generator.configure(job, Integer.parseInt(args[1]));

        job.setOutputFormatClass(TextOutputFormat.class);

        //FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2]));

        int code = job.waitForCompletion(true) ? 0 : 1;
        System.exit(code);
    }

    public static class PubMedGeneratorInputFormat extends DataGenerator.DataGeneratorInputFormat<NullWritable, Text> {

        private final MedlineGenerator generator = new MedlineGenerator(Resources.toString(Resources.getResource("exampleSeed.txt"), Charsets.UTF_8));
        private Text value = new Text();

        public PubMedGeneratorInputFormat() throws IOException {
            super();
        }

        @Override
        public DataGenerator.DataGeneratorRecordReader<NullWritable, Text> getRecordReader() {
            return new DataGenerator.DataGeneratorRecordReader<NullWritable, Text>() {
                @Override
                public Pair<NullWritable, Text> generateRecord() {
                    value.set(generator.generateEntry());
                    return new Pair<NullWritable, Text>(NullWritable.get(), value);
                }
            };
        }
    }


}
