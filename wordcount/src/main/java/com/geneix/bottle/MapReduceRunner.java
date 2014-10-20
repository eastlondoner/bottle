package com.geneix.bottle;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.lang.reflect.Method;

/**
 * Created by andrew on 20/10/14.
 */
public class MapReduceRunner {
    public static void main(String[] args) throws Exception {

        Method main = Class.forName(args[0]).getMethod("main",String[].class);
        main.invoke(args);
    }
}
