package com.geneix.bottle;

import org.apache.hadoop.io.Text;

/**
 * Created by andrew on 20/10/14.
 */
public class FakeWordParser implements WordParser {
    public FakeWordParser(){
        //empty constructor
    }

    @Override
    public Word.WordType interpret(Text text) {
        if (text.getLength() < 3) {
            return Word.WordType.OTHER;
        }
        if (text.getLength() < 4) {
            return Word.WordType.NOUN;
        }
        if (text.getLength() < 5) {
            return Word.WordType.VERB;
        }
        if (text.getLength() < 6) {
            return Word.WordType.ADVERB;
        }
        if (text.getLength() < 7) {
            return Word.WordType.ADJECTIVE;
        }

        return Word.WordType.UNKNOWN;
    }
}
