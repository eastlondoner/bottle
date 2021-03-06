package com.geneix.bottle;

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

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Created by andrew on 06/10/14.
 */
public class WordCount2 {

    //Need a tokenizer
    public static class CustomFileInputFormat extends FileInputFormat<LongWritable,Word>{
        @Override
        public RecordReader<LongWritable, Word> createRecordReader(
                InputSplit split, TaskAttemptContext context) throws IOException,
                InterruptedException {
            return new WordRecordReader();
        }
    }

    public static class Map extends Mapper<LongWritable, Word, Word, IntWritable> {
        private final static IntWritable one = new IntWritable(1);

        public void map(LongWritable key, Word word, Context context) throws IOException, InterruptedException {
            context.write(word,one);
        }
    }

    public static class Reduce extends Reducer<Word, IntWritable, Word, IntWritable> {

        public void reduce(Word key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            context.write(key, new IntWritable(sum));
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "wordcount2");

        job.setJarByClass(WordCount2.class);
        job.setOutputKeyClass(Word.class);
        job.setOutputValueClass(IntWritable.class);

        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);

        job.setInputFormatClass(CustomFileInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2]));

        int code = job.waitForCompletion(true) ? 0 : 1;
        System.exit(code);

    }
}
