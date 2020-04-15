package ru.olegcherednik.zip4jvm.io.ed;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;
import java.io.InputStream;

/**
 * Deflate64 decompressor.
 *
 * @NotThreadSafe
 * @since 1.16
 */
public class Deflate64CompressorInputStream extends InputStream {

    private final HuffmanDecoder decoder;
    private final byte[] oneByte = new byte[1];

    /**
     * Constructs a Deflate64CompressorInputStream.
     *
     * @param in the stream to read from
     */
    public Deflate64CompressorInputStream(DataInput in) {
        decoder = new HuffmanDecoder(in);
    }

    @Override
    public int read() throws IOException {
        while (true) {
            int r = read(oneByte);
            switch (r) {
                case 1:
                    return oneByte[0] & 0xFF;
                case -1:
                    return -1;
                case 0:
                    continue;
                default:
                    throw new IllegalStateException("Invalid return value from read: " + r);
            }
        }
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return decoder.decode(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        decoder.close();
    }

}
