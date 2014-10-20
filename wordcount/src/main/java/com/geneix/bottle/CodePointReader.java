package com.geneix.bottle;

/**
 * Created by andrew on 07/10/14.
 * THIS IS NOT THREAD SAFE
 */
import org.apache.hadoop.fs.FSDataInputStream;

import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;

public class CodePointReader implements Closeable {
    private final static int BYTE_BUFFER_SIZE = 128;
    private final FSDataInputStream in;
    private final ReUsableByteArrayInputStream inStream;
    private final InputStreamReader charSource;
    private int lookAhead;
    private final byte[] byteBuffer;

    //This maps the codepoint array to the byte position in the source file
    private long[] bytePositions;

    //Number of real bytes in the buffer
    private int bufLength;

    private long bytesRead = 0;

    private void lookAhead() throws IOException {
        lookAhead = charSource.read();
        if(lookAhead == -1){
            //We're either at the end of the buffer or nthe end of the file

            //Try refilling the buffer
            bufLength = in.read(byteBuffer);
            if(bufLength <= 0){
                //EOF
                return;
            } else {
                inStream.resetBufferAndLength(bufLength);
                lookAhead = charSource.read();
            }
        }
    }

    public long getBytePosition(int n){
        return bytePositions[n];
    }

    public CodePointReader(FSDataInputStream fileStream, int start) throws IOException {
        this.in = fileStream;
        byteBuffer = new byte[BYTE_BUFFER_SIZE];
        bufLength = in.read(byteBuffer);
        inStream = new ReUsableByteArrayInputStream(byteBuffer);
        this.charSource = new InputStreamReader( inStream, "UTF8");
        lookAhead();
    }

    //This reads chars one at a time and outputs their codepoint
    private int read() throws IOException {
        if(lookAhead == -1){
            return -1;
        }
        try {
            char high = (char) lookAhead;
            if (Character.isHighSurrogate(high)) {
                lookAhead();
                int next = lookAhead;
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
            bytesRead = inStream.totalBytesRead();
            lookAhead();
        }
    }

    //This reads codepoints into buffer array and returns the number of fresh codepoints read in the buffer
    public long read(int[] buff) throws IOException {
        if(bytePositions == null || bytePositions.length < buff.length){
            bytePositions = new long[buff.length];
        }
        for (int i=0; i<buff.length; i++){
            buff[i] = read();
            bytePositions[i] = bytesRead;
            if(buff[i] == -1){
                return i==0? -1 : i+1;
            }
        }
        return buff.length;
    }

    public void close() throws IOException { charSource.close(); }
}