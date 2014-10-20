package com.geneix.bottle;

import com.google.common.base.Charsets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.io.Text;

import java.io.Closeable;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * A class that provides a word reader from an input stream.
 * Words will be terminated by whitespace or end of line chars
 * EOF also terminates an otherwise unterminated
 * word.
 */
public class WordReader implements Closeable {

    private static final Log LOG = LogFactory.getLog(WordReader.class);
    private static final CharsetEncoder UTF_8_ENCODER = Charsets.UTF_8.newEncoder();

    private static final int DEFAULT_BUFFER_SIZE = 64 * 1024;

    // The word delimiters as unicode code point values
    private static final Set<Integer> wordDelimiters = new TreeSet<Integer>(Arrays.asList(
            0x0009,
            0x000A,
            0x000B,
            0x000C,
            0x000D,
            0x0020,
            0x0085,
            0x00A0, // No break space .. maybe not break on this
            0x1680,
            0x2000,
            0x2001,
            0x2002,
            0x2003,
            0x2004,
            0x2005,
            0x2006,
            0x2007,
            0x2008,
            0x2009,
            0x200A,
            0x2028,
            0x2029,
            0x202F,
            0x205F,
            0x3000
    ));
    private CodePointReader in;
    private int[] buffer;

    //The capacity of the buffer;
    private int bufferSize = DEFAULT_BUFFER_SIZE;

    // the number of chars of real data in the buffer
    private long bufferLength = 0;

    // the current position in the buffer in codepoints
    private int bufferPosn = 0;
    // the current position in the buffer in bytes
    private long bufferBytePosn = 0;

    /**
     * Create a line reader that reads from the given stream using the
     * default buffer-size (64k).
     *
     * @param in The input stream
     * @throws IOException
     */
    public WordReader(FSDataInputStream in) throws IOException {
        this(in, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Create a line reader that reads from the given stream using the
     * given buffer-size.
     *
     * @param in         The input stream
     * @param bufferSize Size of the read buffer
     * @throws IOException
     */
    public WordReader(FSDataInputStream in, int bufferSize) throws IOException {
        this.in = new CodePointReader(in, bufferSize);
        this.bufferSize = bufferSize;
        this.buffer = new int[this.bufferSize];
    }


    /**
     * Create a line reader that reads from the given stream using the
     * <code>io.file.buffer.size</code> specified in the given
     * <code>Configuration</code>.
     *
     * @param in   input stream
     * @param conf configuration
     * @throws IOException
     */
    public WordReader(FSDataInputStream in, Configuration conf) throws IOException {
        this(in, conf.getInt("io.file.buffer.size", DEFAULT_BUFFER_SIZE));
    }

    protected static long fillBuffer(CodePointReader in, int[] buffer) throws IOException {
        return in.read(buffer);
    }

    /**
     * Read one word from the InputStream into the given Text.
     *
     * @param str               the object to store the given word (without whitespace)
     * @param maxLineLength     the maximum number of codepoints to store into str;
     *                          the rest of the line is silently discarded.
     * @param maxBytesToConsume the maximum number of codepoints to consume
     *                          in this call.  This is only a hint, because if the line cross
     *                          this threshold, we allow it to happen.  It can overshoot
     *                          potentially by as much as one buffer length.
     * @return the number of codepoints read including the whitespace
     * @throws IOException if the underlying stream throws
     */
    public int readWord(Text str, int maxLineLength,
                        int maxBytesToConsume) throws IOException {
        return readCustomLine(str, maxLineLength, maxBytesToConsume);
    }


    /**
     * Read a word terminated by whitespace delimiter.
     */
    private int readCustomLine(Text str, int maxWordLength, int maxBytesToConsume)
            throws IOException {
        if(LOG.isInfoEnabled()){
            LOG.info("Read Word Called");
        }
        str.clear();
        int txtLength = 0; // tracks str.getLength(), as an optimization
        long bytesConsumed = 0;
        boolean terminatingSpaceReached = false;
        do {
            long startBytes = bufferBytePosn;
            int wordStart = bufferPosn;
            if (bufferPosn >= bufferLength) {
                wordStart = bufferPosn = 0;
                bufferLength = fillBuffer(in, buffer);

                if(LOG.isInfoEnabled()){
                    LOG.info(String.format("Buffer length: %s",bufferLength));
                }

                startBytes = in.getBytePosition(bufferPosn);
                if (bufferLength <= 0) {
                    if(LOG.isInfoEnabled()){
                        LOG.info("EOF Reached");
                    }
                    break; // EOF
                }
            }
            //First read past any leading whitespace
            for (; bufferPosn < bufferLength; ++bufferPosn) {
                if (!wordDelimiters.contains(buffer[bufferPosn])) {
                    if(LOG.isInfoEnabled()){
                        LOG.info("Found a non-whitespace codepoint.");
                        LOG.info(String.format("Buffer position: %s",bufferPosn));
                    }
                    wordStart = bufferPosn;
                    bufferPosn++;
                    break;
                }
                if(txtLength > 0){
                    //we found a space after refulling buffer
                    terminatingSpaceReached = true;
                    break;
                }
            }

            //Now read all characters until delimiter
            for (; bufferPosn < bufferLength; ++bufferPosn) {
                if (wordDelimiters.contains(buffer[bufferPosn])) {
                    if(LOG.isInfoEnabled()){
                        LOG.info("Found a whitespace");
                        LOG.info(String.format("Buffer position: %s",bufferPosn));
                    }
                    terminatingSpaceReached = true;
                    bufferPosn++;
                    break;
                }
            }
            bytesConsumed = in.getBytePosition(bufferPosn -1)-startBytes; //The last char read is the one before the buffer position

            if(LOG.isInfoEnabled()){
                LOG.info(String.format("Bytes consumed: %s",bytesConsumed));
            }

            int appendLength = bufferPosn-1 - wordStart;  //The last char read is the one before the buffer position
            if (appendLength > maxWordLength - txtLength) {
                appendLength = maxWordLength - txtLength;
            }
            if(LOG.isInfoEnabled()){
                LOG.info(String.format("Append Length: %s",appendLength));
            }
            if (appendLength > 0) {
                str.append(new String(buffer, wordStart, appendLength).getBytes(), 0, appendLength); //TODO get rid of this cast to string ?
                txtLength += appendLength;
            }
        } while (!terminatingSpaceReached
                && bytesConsumed < maxBytesToConsume);
        if (bytesConsumed > Integer.MAX_VALUE) {
            throw new IOException("Too many bytes before delimiter: " + bytesConsumed);
        }
        if(LOG.isInfoEnabled()){
            LOG.info(String.format("Word found: %s",str.toString()));
        }
        bufferBytePosn = in.getBytePosition(bufferPosn - 1);
        return (int) bytesConsumed;
    }

    /**
     * Read from the InputStream into the given Text.
     *
     * @param str           the object to store the given line
     * @param maxLineLength the maximum number of bytes to store into str.
     * @return the number of bytes read including the newline
     * @throws IOException if the underlying stream throws
     */
    public int readWord(Text str, int maxLineLength) throws IOException {
        return readWord(str, maxLineLength, Integer.MAX_VALUE);
    }

    /**
     * Read from the InputStream into the given Text.
     *
     * @param str the object to store the given line
     * @return the number of bytes read including the newline
     * @throws IOException if the underlying stream throws
     */
    public int readWord(Text str) throws IOException {
        return readWord(str, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}
