package ru.olegcherednik.zip4jvm.io.lzma.xz;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.lzma.xz.lzma.LzmaEncoder;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Compresses into the legacy .lzma file format or into a raw LZMA stream.
 *
 * @since 1.6
 */
public class LzmaOutputStream extends OutputStream {

    private final DataOutput out;
    private final LzmaEncoder lzma;

    private final long uncompressedSize;
    private long currentUncompressedSize;

    private boolean finished;

    private final byte[] tempBuf = new byte[1];
    private final LzmaInputStream.Properties properties;

    /**
     * Creates a new compressor for the legacy .lzma file format.
     * <p>
     * This is identical to
     * <code>LZMAOutputStream(OutputStream, LZMA2Options, long)</code>
     * except that this also takes the <code>arrayCache</code> argument.
     *
     * @param out              output stream to which the compressed data
     *                         will be written
     * @param properties       LZMA compression options; the same class
     *                         is used here as is for LZMA2
     * @param uncompressedSize uncompressed size of the data to be compressed;
     *                         use <code>-1</code> when unknown
     * @throws IOException may be thrown from <code>out</code>
     * @since 1.7
     */
    public LzmaOutputStream(DataOutput out, LzmaInputStream.Properties properties, long uncompressedSize) throws IOException {
        this.out = out;
        this.properties = properties;
        this.uncompressedSize = uncompressedSize;
        lzma = properties.getMode().createEncoder(out, properties);
    }

    public void writeHeader() throws IOException {
        properties.write(out);
    }

    /**
     * Gets the amount of uncompressed data written to the stream.
     * This is useful when creating raw LZMA streams without
     * the end of stream marker.
     */
    public long getUncompressedSize() {
        return currentUncompressedSize;
    }

    public void write(int b) throws IOException {
        tempBuf[0] = (byte)b;
        write(tempBuf, 0, 1);
    }

    public void write(byte[] buf, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len < 0 || off + len > buf.length)
            throw new IndexOutOfBoundsException();

        if (finished)
            throw new IOException("Stream finished or closed");

        if (uncompressedSize != -1
                && uncompressedSize - currentUncompressedSize < len)
            throw new IOException("Expected uncompressed input size ("
                    + uncompressedSize + " bytes) was exceeded");

        currentUncompressedSize += len;

        while (len > 0) {
            int used = lzma.getLZEncoder().fillWindow(buf, off, len);
            off += used;
            len -= used;
            lzma.encodeForLZMA1();
        }
    }

    /**
     * Finishes the stream without closing the underlying OutputStream.
     */
    public void finish() throws IOException {
        if (!finished) {
            if (uncompressedSize != -1
                    && uncompressedSize != currentUncompressedSize)
                throw new IOException("Expected uncompressed size ("
                        + uncompressedSize + ") doesn't equal "
                        + "the number of bytes written to the stream ("
                        + currentUncompressedSize + ")");

            lzma.getLZEncoder().setFinishing();
            lzma.encodeForLZMA1();

            if (uncompressedSize == -1)
                lzma.encodeLZMA1EndMarker();

            lzma.finish();
            finished = true;
        }
    }

    /**
     * Finishes the stream and closes the underlying OutputStream.
     */
    public void close() throws IOException {
        try {
            finish();
        } catch(IOException e) {
        }

        out.close();
    }

    @Getter
    @Builder
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Properties {

        /**
         * The largest dictionary size supported by this implementation.
         * <p>
         * LZMA allows dictionaries up to one byte less than 4 GiB. This implementation supports only 16 bytes less than 2 GiB. This limitation is
         * due
         * to Java using signed 32-bit integers for array indexing. The limitation shouldn't matter much in practice since so huge dictionaries are
         * not
         * normally used.
         */
        public static final int DICTIONARY_SIZE_MAX = Integer.MAX_VALUE & ~15;
        public static final int DICTIONARY_SIZE_MIN = 4096;

        private final int lc; // literal context bits
        private final int lp; // literal position bits
        private final int pb; // position bits
        private final int dictionarySize;

        public int write(DataOutput out) throws IOException {
            out.writeByte((byte)((pb * 5 + lp) * 9 + lc));
            out.writeDword(dictionarySize);
            return 5;
        }

        public static Properties read(DataInput in) throws IOException {
            int v = in.readByte() & 0xFF;
            int lc = v % 9;
            int lp = (v / 9) % 5;
            int pb = v / (9 * 5);
            int dictionarySize = (int)in.readDword();

            checkDictionarySize(dictionarySize);

            return new Properties(lc, lp, pb, dictionarySizeInRange(dictionarySize));
        }

        private static void checkDictionarySize(int dictionarySize) {
            if (dictionarySize < 0)
                throw new IllegalArgumentException("Incorrect LZMA dictionary size: " + dictionarySize);
            if (dictionarySize > DICTIONARY_SIZE_MAX)
                throw new IllegalArgumentException("Incorrect LZMA dictionary size is too big for this implementation: " + dictionarySize);
        }

        private static int dictionarySizeInRange(int dictionarySize) {
            return Math.max(DICTIONARY_SIZE_MIN, Math.min(dictionarySize, DICTIONARY_SIZE_MAX));
        }

    }

}
