package com.geneix.bottle;

import org.apache.commons.math3.util.Pair;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.log4j.Logger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class based on MapReduce Design Patterns (O'Reilly 2012) example source code
 */
public class DataGenerator<GEN extends DataGenerator.DataGeneratorInputFormat> {
    private final static Logger LOG = Logger.getLogger(DataGenerator.class);

    public static final String NUM_MAP_TASKS = "random.generator.map.tasks";
    public static final String NUM_RECORDS_PER_TASK = "random.generator.num.records.per.map.task";
    public static final String RANDOM_WORD_LIST = "random.generator.random.word.file";
    private final GEN gen;

    public DataGenerator(GEN implementation){
        gen = implementation;
    }

    public void configure(Job job, int approxTotalRecords) throws IOException, InterruptedException {
        ClusterStatus clusterStatus = new JobClient(new JobConf()).getClusterStatus();
        int nodes = clusterStatus.getTaskTrackers();
        int mapRedSlots = clusterStatus.getMaxMapTasks();

        int recordsPerSlot = Math.round(approxTotalRecords / mapRedSlots);

        DataGenerator.DataGeneratorInputFormat.setNumMapTasks(job, mapRedSlots);
        DataGenerator.DataGeneratorInputFormat.setNumRecordPerTask(job, recordsPerSlot);

        if(LOG.isInfoEnabled()){
            LOG.info(String.format("SPLITS: %s; RECORDS PER SPLIT: %s; TOTAL RECORDS: %s", mapRedSlots, recordsPerSlot, recordsPerSlot*mapRedSlots));
        }

        job.setNumReduceTasks(0);
        job.setInputFormatClass(gen.getClass());
        job.setOutputKeyClass(gen.getKeyClass());
        job.setOutputValueClass(gen.getValueClass());
    }

    public static abstract class DataGeneratorRecordReader<KEYIN, VALUEIN> extends
            RecordReader<KEYIN, VALUEIN> {

        private int numRecordsToCreate = 0;
        private int createdRecords = 0;
        private KEYIN key;
        private VALUEIN value;

        public abstract Pair<KEYIN, VALUEIN> generateRecord();

        @Override
        public void initialize(InputSplit split, TaskAttemptContext context)
                throws IOException, InterruptedException {

            // Get the number of records to create from the configuration
            this.numRecordsToCreate = context.getConfiguration().getInt(
                    NUM_RECORDS_PER_TASK, -1);

            if (numRecordsToCreate < 0) {
                throw new InvalidParameterException(NUM_RECORDS_PER_TASK
                        + " is not set.");
            }
        }

        @Override
        public boolean nextKeyValue() throws IOException,
                InterruptedException {
            // If we still have records to create
            if (createdRecords < numRecordsToCreate) {
                Pair<KEYIN, VALUEIN> record = generateRecord();
                key = record.getFirst();
                value = record.getSecond();
                ++createdRecords;
                return true;
            } else {
                // Else, return false
                return false;
            }
        }

        @Override
        public KEYIN getCurrentKey() throws IOException,
                InterruptedException {
            return key;
        }

        @Override
        public VALUEIN getCurrentValue() throws IOException,
                InterruptedException {
            return value;
        }

        @Override
        public float getProgress() throws IOException, InterruptedException {
            return (float) createdRecords / (float) numRecordsToCreate;
        }

        @Override
        public void close() throws IOException {
            // nothing to do here...
        }
    }

    public abstract static class DataGeneratorInputFormat<KEYIN, VALUEIN> extends
            InputFormat<KEYIN, VALUEIN> {

        public abstract DataGeneratorRecordReader<KEYIN, VALUEIN> getRecordReader();

        public static void setNumMapTasks(Job job, int i) {
            job.getConfiguration().setInt(NUM_MAP_TASKS, i);
        }

        public static void setNumRecordPerTask(Job job, int i) {
            job.getConfiguration().setInt(NUM_RECORDS_PER_TASK, i);
        }

        @Override
        public List<InputSplit> getSplits(JobContext job) throws IOException {

            // Get the number of map tasks configured
            int numSplits = job.getConfiguration().getInt(NUM_MAP_TASKS, -1);
            if (numSplits <= 0) {
                throw new IOException(NUM_MAP_TASKS + " is not set.");
            }

            // Create a number of input splits equivalent to the number of tasks
            ArrayList<InputSplit> splits = new ArrayList<InputSplit>();
            for (int i = 0; i < numSplits; ++i) {
                splits.add(new FakeInputSplit());
            }

            return splits;
        }

        @Override
        public RecordReader<KEYIN, VALUEIN> createRecordReader(
                InputSplit split, TaskAttemptContext context)
                throws IOException, InterruptedException {
            // Create a new RandomStackoverflowRecordReader and initialize it
            DataGeneratorRecordReader<KEYIN, VALUEIN> rr = getRecordReader();
            rr.initialize(split, context);
            return rr;
        }

        public abstract Class<KEYIN> getKeyClass();
        public abstract Class<VALUEIN> getValueClass();

        /**
         * This class is very empty.
         */
        public static class FakeInputSplit extends InputSplit implements
                Writable {

            @Override
            public void readFields(DataInput arg0) throws IOException {
            }

            @Override
            public void write(DataOutput arg0) throws IOException {
            }

            @Override
            public long getLength() throws IOException, InterruptedException {
                return 0;
            }

            @Override
            public String[] getLocations() throws IOException,
                    InterruptedException {
                return new String[0];
            }
        }
    }
}
