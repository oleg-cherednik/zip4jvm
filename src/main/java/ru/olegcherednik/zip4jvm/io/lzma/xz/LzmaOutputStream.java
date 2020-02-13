package ru.olegcherednik.zip4jvm.io.lzma.xz;

import ru.olegcherednik.zip4jvm.io.lzma.xz.lzma.LZMAEncoder;
import ru.olegcherednik.zip4jvm.io.lzma.xz.rangecoder.RangeEncoderToStream;
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

    private final ArrayCache arrayCache = ArrayCache.getDefaultCache();

    private final RangeEncoderToStream rc;
    private LZMAEncoder lzma;

    private final int props;
    private final long uncompressedSize;
    private long currentUncompressedSize = 0;

    private boolean finished;
    private IOException exception;

    private final byte[] tempBuf = new byte[1];
    private final int dictSize;

    /**
     * Creates a new compressor for the legacy .lzma file format.
     * <p>
     * This is identical to
     * <code>LZMAOutputStream(OutputStream, LZMA2Options, long)</code>
     * except that this also takes the <code>arrayCache</code> argument.
     *
     * @param out              output stream to which the compressed data
     *                         will be written
     * @param options          LZMA compression options; the same class
     *                         is used here as is for LZMA2
     * @param uncompressedSize uncompressed size of the data to be compressed;
     *                         use <code>-1</code> when unknown
     * @throws IOException may be thrown from <code>out</code>
     * @since 1.7
     */
    public LzmaOutputStream(DataOutput out, LZMA2Options options, long uncompressedSize) throws IOException {
        this.out = out;
        rc = new RangeEncoderToStream(out);
        this.uncompressedSize = uncompressedSize;

        dictSize = options.getDictionarySize();
        lzma = LZMAEncoder.getInstance(rc,
                options.getLc(), options.getLp(), options.getPb(),
                options.getMode(),
                dictSize, 0, options.getNiceLength(),
                options.getMatchFinder(), options.getDepthLimit(),
                arrayCache);

        props = (options.getPb() * 5 + options.getLp()) * 9 + options.getLc();
    }

    public void writeHeader() throws IOException {
        out.writeByte(props);
        out.writeDword(dictSize);
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

        if (exception != null)
            throw exception;

        if (finished)
            throw new XZIOException("Stream finished or closed");

        if (uncompressedSize != -1
                && uncompressedSize - currentUncompressedSize < len)
            throw new XZIOException("Expected uncompressed input size ("
                    + uncompressedSize + " bytes) was exceeded");

        currentUncompressedSize += len;

        try {
            while (len > 0) {
                int used = lzma.getLZEncoder().fillWindow(buf, off, len);
                off += used;
                len -= used;
                lzma.encodeForLZMA1();
            }
        } catch(IOException e) {
            exception = e;
            throw e;
        }
    }

    /**
     * Flushing isn't supported and will throw XZIOException.
     */
    public void flush() throws IOException {
        throw new XZIOException("LZMAOutputStream does not support flushing");
    }

    /**
     * Finishes the stream without closing the underlying OutputStream.
     */
    public void finish() throws IOException {
        if (!finished) {
            if (exception != null)
                throw exception;

            try {
                if (uncompressedSize != -1
                        && uncompressedSize != currentUncompressedSize)
                    throw new XZIOException("Expected uncompressed size ("
                            + uncompressedSize + ") doesn't equal "
                            + "the number of bytes written to the stream ("
                            + currentUncompressedSize + ")");

                lzma.getLZEncoder().setFinishing();
                lzma.encodeForLZMA1();

                if (uncompressedSize == -1)
                    lzma.encodeLZMA1EndMarker();

                rc.finish();
            } catch(IOException e) {
                exception = e;
                throw e;
            }

            finished = true;

            lzma.putArraysToCache(arrayCache);
            lzma = null;
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

        try {
            out.close();
        } catch(IOException e) {
            if (exception == null)
                exception = e;
        }

        if (exception != null)
            throw exception;
    }


}
