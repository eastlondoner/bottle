package com.geneix.bottle;

/**
 * Created by andrew on 07/10/14.
 */
import java.io.*;
public class CodePointReader implements Closeable {
    private final InputStreamReader charSource;
    private int lookAhead;

    public CodePointReader(InputStreamReader charSource) throws IOException {
        this.charSource = charSource;
        lookAhead = charSource.read();
    }

    public int read() throws IOException {
        if(lookAhead == -1){
            return -1;
        }
        try {
            char high = (char) lookAhead;
            if (Character.isHighSurrogate(high)) {
                int next = charSource.read();
                if (next == -1) { throw new IOException("malformed character"); }
                char low = (char) next;
                if(!Character.isLowSurrogate(low)) {
                    throw new IOException("malformed sequence");
                }
                return Character.toCodePoint(high, low);
            } else {
                return lookAhead;
            }
        } finally {
            lookAhead = charSource.read();
        }
    }

    public int read(int[] buff) throws IOException {
        for (int i=0; i<buff.length; i++){
            buff[i] = read();
            if(buff[i] == -1){
                return i==0? -1 : i+1;
            }
        }
        return buff.length;
    }

    public void close() throws IOException { charSource.close(); }
}