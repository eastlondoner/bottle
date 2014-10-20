package com.geneix.bottle;

import java.io.ByteArrayInputStream;

/**
 * Created by andrew on 08/10/14.
 * THIS IS NOT THREAD SAFE
 */
public class ReUsableByteArrayInputStream extends ByteArrayInputStream {
    private long bytesRead = 0;
    public ReUsableByteArrayInputStream(byte[] buf) {
        super(buf);
    }

    public ReUsableByteArrayInputStream(byte[] buf, int offset, int length) {
        super(buf, offset, length);
    }

    public void resetBufferAndLength(int newLength){
        bytesRead+=pos;
        reset();
        count = newLength;
    }

    //This isn't strictly the number of bytes read
    public long totalBytesRead(){
        return bytesRead+pos;
    }
}
