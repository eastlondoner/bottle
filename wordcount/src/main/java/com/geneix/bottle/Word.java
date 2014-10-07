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
public class Word implements WritableComparable<Word>{

    private WordType wordType;
    private Text txt;

    public Word(Text word, WordType type){
        wordType = type;
        txt = word;
    }

    @Override
    public int hashCode(){
        return 4294967 * wordType.ordinal() + txt.hashCode(); //That's a prime which is approx (2^32) / 10
    }

    @Override
    public int compareTo(Word word) {
        int thisLength = this.txt.getLength();
        int thatLength = word.txt.getLength();
        return thatLength < thisLength ? -1 : thatLength > thisLength ? 1 : 0;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        txt.write(out);
        WritableUtils.writeEnum(out,wordType);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        txt.readFields(in);
        wordType = WritableUtils.readEnum(in,WordType.class);
    }

    public static enum WordType {
        UNKNOWN,
        NOUN,
        VERB,
        ADJECTIVE,
        ADVERB,
        OTHER; //Other is for words like and, so, if etc.
    }
}
