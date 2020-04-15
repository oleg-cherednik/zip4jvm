package ru.olegcherednik.zip4jvm.io.ed;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Deflate64 decompressor.
 *
 * @NotThreadSafe
 * @since 1.16
 */
public class Deflate64CompressorInputStream extends CompressorInputStream implements InputStreamStatistics {

    private InputStream originalStream;
    private HuffmanDecoder decoder;
    private long compressedBytesRead;
    private final byte[] oneByte = new byte[1];

    /**
     * Constructs a Deflate64CompressorInputStream.
     *
     * @param in the stream to read from
     */
    public Deflate64CompressorInputStream(InputStream in) {
        this(new HuffmanDecoder(in));
        originalStream = in;
    }

    Deflate64CompressorInputStream(HuffmanDecoder decoder) {
        this.decoder = decoder;
    }

    /**
     * @throws java.io.EOFException if the underlying stream is exhausted before the end of defalted data was reached.
     */
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

    /**
     * @throws java.io.EOFException if the underlying stream is exhausted before the end of deflated data was reached.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        int read = -1;
        if (decoder != null) {
            read = decoder.decode(b, off, len);
            compressedBytesRead = decoder.getBytesRead();
            count(read);
            if (read == -1) {
                closeDecoder();
            }
        }
        return read;
    }

    @Override
    public int available() throws IOException {
        return decoder != null ? decoder.available() : 0;
    }

    @Override
    public void close() throws IOException {
        try {
            closeDecoder();
        } finally {
            if (originalStream != null) {
                originalStream.close();
                originalStream = null;
            }
        }
    }

    /**
     * @since 1.17
     */
    @Override
    public long getCompressedCount() {
        return compressedBytesRead;
    }

    private void closeDecoder() {
        closeQuietly(decoder);
        decoder = null;
    }

    public static void closeQuietly(final Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch(final IOException ignored) { // NOPMD NOSONAR
            }
        }
    }
}
