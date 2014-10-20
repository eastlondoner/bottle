package com.geneix.bottle;

/**
 * Created by andrew on 07/10/14.
 * THIS IS NOT THREAD SAFE
 */

import com.google.common.base.Charsets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FSDataInputStream;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

public class CodePointReader implements Closeable {
    private static final Log LOG = LogFactory.getLog(CodePointReader.class);
    private final static int BYTE_BUFFER_SIZE = 128;

    private final CharsetDecoder decoder = Charsets.UTF_8.newDecoder();
    private final byte[] lookAheadByteBuffer = new byte[1];
    private final CharBuffer lookAheadCharOut = CharBuffer.wrap(new char[1]);
    private final FSDataInputStream in;
    private final ReUsableByteArrayInputStream inStream;
    private final byte[] byteBuffer;
    private int lookAhead;
    //This maps the codepoint array to the byte position in the source file
    private long[] bytePositions;

    //Number of real bytes in the buffer
    private int bufLength;

    private long bytesRead = 0;

    //TODO: use buffer size arg
    public CodePointReader(FSDataInputStream fileStream, int bufferSize) throws IOException {
        this.in = fileStream;
        byteBuffer = new byte[BYTE_BUFFER_SIZE];
        bufLength = in.read(byteBuffer);
        inStream = new ReUsableByteArrayInputStream(byteBuffer);
        lookAhead();
    }

    private void lookAhead() throws IOException {
        CoderResult result = null;
        int n = 0;
        do {
            if (inStream.read(lookAheadByteBuffer) == -1) {
                //Try refilling the buffer
                bufLength = in.read(byteBuffer);
                if (bufLength <= 0) {
                    //EOF
                    lookAhead = -1;
                    return;
                } else {
                    inStream.resetBufferAndLength(bufLength);
                    inStream.read(lookAheadByteBuffer);
                }
            }
            result = decoder.decode(ByteBuffer.wrap(lookAheadByteBuffer), lookAheadCharOut, false);
            n++;
        } while (result.isUnderflow() && n < 6);
        if (n > 6) {
            throw new IOException("Cannot have more than 6 bytes in a UFT8 character");
        }

        if (result.isUnderflow() || result.isMalformed() || result.isOverflow() || result.isError()) {
            throw new IOException("Error decoding UTF8");
        }
        lookAhead = lookAheadCharOut.get(0);
        lookAheadCharOut.reset();
    }

    public long getBytePosition(int n) {
        LOG.info(String.format("Getting byte position corresponding to buffer position %s ... %s", n, bytePositions[n]));
        return bytePositions[n];
    }

    //This reads chars one at a time and outputs their codepoint
    private int read() throws IOException {
        if (lookAhead == -1) {
            return -1;
        }
        try {
            char high = (char) lookAhead;
            if (Character.isHighSurrogate(high)) {
                lookAhead();
                int next = lookAhead;
                if (next == -1) {
                    throw new IOException("malformed character");
                }
                char low = (char) next;
                if (!Character.isLowSurrogate(low)) {
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
        if (bytePositions == null || bytePositions.length < buff.length) {
            bytePositions = new long[buff.length];
        }
        for (int i = 0; i < buff.length; i++) {
            buff[i] = read();
            bytePositions[i] = bytesRead;
            if (buff[i] == -1) {
                return i == 0 ? -1 : i + 1;
            }
        }
        return buff.length;
    }

    public void close() throws IOException {
        inStream.close();
    }
}