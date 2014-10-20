package com.geneix.bottle;

import org.apache.hadoop.io.Text;

/**
 * Created by andrew on 20/10/14.
 */
public interface WordParser {
    public Word.WordType interpret(Text text);
}
