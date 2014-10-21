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

    private int length;
    private Text txt;
    private WordType wordType;

    //Empty constructor required for Hadoop?
    public Word(){
        txt = new Text();
    }

    private Word(Text word, WordType type){
        txt = word;
        length = word.getLength();
        wordType = type;
    }

    public int getLength(){
        return txt.getLength();
    }


    @Override
    public int hashCode(){
        return 4294967 * wordType.ordinal() + txt.hashCode(); //That's a prime which is approx (2^32) / 10
    }

    @Override
    public int compareTo(Word word) {
        int thisLength = this.txt.getLength();
        int thatLength = word.txt.getLength();
        if(thatLength != thisLength){
            return thatLength < thisLength ? -1 : thatLength > thisLength ? 1 : 0;
        }
        return this.txt.compareTo(word.txt);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        txt.write(out);
        WritableUtils.writeEnum(out, wordType);
        WritableUtils.writeVInt(out, length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Word word = (Word) o;

        if (length != word.length) return false;
        if (!txt.equals(word.txt)) return false;
        if (wordType != word.wordType) return false;

        return true;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        txt.readFields(in);
        wordType = WritableUtils.readEnum(in,WordType.class);
        length = WritableUtils.readVInt(in);
    }

    @Override
    public String toString() {
        return "Word{" +
                "length=" + length +
                ", txt=" + txt +
                ", wordType=" + wordType +
                '}';
    }

    public static enum WordType {
        UNKNOWN,
        NOUN,
        VERB,
        ADJECTIVE,
        ADVERB,
        OTHER; //Other is for words like and, so, if etc.
    }

    public static Word fromText(Text word, WordParser parser){
        return new Word(word, parser.interpret(word));
    }
}
