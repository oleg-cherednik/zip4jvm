package com.cop.zip4j.io.in.entry.old;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class PartInputStream extends InputStream {

    @NonNull
    private DataInput in;
    private final long length;
    private final Decoder decoder;
    private final ZipModel zipModel;

    private long bytesRead;
    private byte[] oneByteBuff = new byte[1];

    @Override
    public int available() {
        return (int)Math.min(length - bytesRead, Integer.MAX_VALUE);
    }

    @Override
    public int read() throws IOException {
        if (bytesRead >= length)
            return -1;

        return read(oneByteBuff, 0, 1) == -1 ? -1 : oneByteBuff[0] & 0xFF;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        if (len > length - bytesRead) {
            len = (int)(length - bytesRead);

            if (len == 0) {
                return -1;
            }
        }

        len = decoder.getLen(bytesRead, len, length);


        int count = in.read(buf, offs, len);

        if (count > 0) {
            decoder.decrypt(buf, offs, count);
            bytesRead += count;
        }

        return count;
    }

    @Override
    public long skip(long n) throws IOException {
        if (n < 0)
            throw new IllegalArgumentException();
        if (n > length - bytesRead)
            n = length - bytesRead;
        bytesRead += n;
        return n;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

}
