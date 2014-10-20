package com.geneix.bottle;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by andrew on 06/10/14.
 */
public class Line implements WritableComparable<Line>{

    private int wordCount;
    private Text txt;

    //Empty constructor required for Hadoop?
    public Line(){
        txt = new Text();
    }

    public Line(Text line){
        txt = line;
    }

    public int getLength(){
        return txt.getLength();
    }


    @Override
    public int hashCode(){
        return 4294967 * wordCount + txt.hashCode(); //That's a prime which is approx (2^32) / 10
    }

    @Override
    public int compareTo(Line line) {
        int thisLength = this.txt.getLength();
        int thatLength = line.txt.getLength();
        return thatLength < thisLength ? -1 : thatLength > thisLength ? 1 : 0;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        txt.write(out);
        WritableUtils.writeVInt(out,wordCount);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        txt.readFields(in);
        wordCount = WritableUtils.readVInt(in);
    }

}
