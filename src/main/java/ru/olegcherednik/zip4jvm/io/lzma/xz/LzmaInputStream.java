package ru.olegcherednik.zip4jvm.io.lzma.xz;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.lzma.xz.exceptions.CorruptedInputException;
import ru.olegcherednik.zip4jvm.io.lzma.xz.exceptions.MemoryLimitException;
import ru.olegcherednik.zip4jvm.io.lzma.xz.exceptions.UnsupportedOptionsException;
import ru.olegcherednik.zip4jvm.io.lzma.xz.lzma.LzmaDecoder;

import java.io.IOException;
import java.io.InputStream;

/**
 * Decompresses legacy .lzma files and raw LZMA streams (no .lzma header).
 * <p>
 * <b>IMPORTANT:</b> In contrast to other classes in this package, this class
 * reads data from its input stream one byte at a time. If the input stream
 * is for example {@link java.io.FileInputStream}, wrapping it into
 * {@link java.io.BufferedInputStream} tends to improve performance a lot.
 * This is not automatically done by this class because there may be use
 * cases where it is desired that this class won't read any bytes past
 * the end of the LZMA stream.
 * <p>
 *
 * @since 1.4
 */
public class LzmaInputStream extends InputStream {

    private final LzmaDecoder lzma;

    private boolean endReached;

    private final byte[] tempBuf = new byte[1];

    /**
     * Number of uncompressed bytes left to be decompressed, or -1 if
     * the end marker is used.
     */
    private long remainingSize;

    /**
     * Creates a new .lzma file format decompressor with an optional
     * memory usage limit.
     * <p>
     * This is identical to <code>LZMAInputStream(InputStream, int)</code>
     * except that this also takes the <code>arrayCache</code> argument.
     *
     * @param in         input stream from which .lzma data is read;
     *                   it might be a good idea to wrap it in
     *                   <code>BufferedInputStream</code>, see the
     *                   note at the top of this page
     * @param uncompSize uncompressed size or <t>-1</t> if unknown
     * @throws CorruptedInputException     file is corrupt or perhaps not in
     *                                     the .lzma format at all
     * @throws UnsupportedOptionsException dictionary size or uncompressed size is too
     *                                     big for this implementation
     * @throws MemoryLimitException        memory usage limit was exceeded
     * @throws IOException                 may be thrown by <code>in</code>
     */
    public LzmaInputStream(DataInput in, long uncompSize) throws IOException {
        LzmaProperties properties = LzmaProperties.read(in);
        lzma = new LzmaDecoder(in, properties);
        remainingSize = uncompSize;
    }

    /**
     * Decompresses the next byte from this input stream.
     * <p>
     * Reading lots of data with <code>read()</code> from this input stream
     * may be inefficient. Wrap it in <code>java.io.BufferedInputStream</code>
     * if you need to read lots of data one byte at a time.
     *
     * @return the next decompressed byte, or <code>-1</code>
     * to indicate the end of the compressed stream
     * @throws CorruptedInputException
     * @throws XZIOException           if the stream has been closed
     * @throws IOException             may be thrown by <code>in</code>
     */
    public int read() throws IOException {
        return read(tempBuf, 0, 1) == -1 ? -1 : (tempBuf[0] & 0xFF);
    }

    /**
     * Decompresses into an array of bytes.
     * <p>
     * If <code>len</code> is zero, no bytes are read and <code>0</code>
     * is returned. Otherwise this will block until <code>len</code>
     * bytes have been decompressed, the end of the LZMA stream is reached,
     * or an exception is thrown.
     *
     * @param buf target buffer for uncompressed data
     * @param off start offset in <code>buf</code>
     * @param len maximum number of uncompressed bytes to read
     * @return number of bytes read, or <code>-1</code> to indicate
     * the end of the compressed stream
     * @throws CorruptedInputException
     * @throws XZIOException           if the stream has been closed
     * @throws IOException             may be thrown by <code>in</code>
     */
    public int read(byte[] buf, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len < 0 || off + len > buf.length)
            throw new IndexOutOfBoundsException();

        if (len == 0)
            return 0;

        if (endReached)
            return -1;

        int size = 0;

        while (len > 0) {
            // If uncompressed size is known and thus no end marker will
            // be present, set the limit so that the uncompressed size
            // won't be exceeded.
            int copySizeMax = len;
            if (remainingSize >= 0 && remainingSize < len)
                copySizeMax = (int)remainingSize;

            lzma.getLz().setLimit(copySizeMax);

            // Decode into the dictionary buffer.
            try {
                lzma.decode();
            } catch(CorruptedInputException e) {
                // The end marker is encoded with a LZMA symbol that
                // indicates maximum match distance. This is larger
                // than any supported dictionary and thus causes
                // CorruptedInputException from LZDecoder.repeat.
                if (remainingSize != -1 || !lzma.endMarkerDetected())
                    throw e;

                endReached = true;

                // The exception makes lzma.decode() miss the last range
                // decoder normalization, so do it here. This might
                // cause an IOException if it needs to read a byte
                // from the input stream.
                lzma.getRc().normalize();
            }

            // Copy from the dictionary to buf.
            int copiedSize = lzma.getLz().flush(buf, off);
            off += copiedSize;
            len -= copiedSize;
            size += copiedSize;

            if (remainingSize >= 0) {
                // Update the number of bytes left to be decompressed.
                remainingSize -= copiedSize;
                assert remainingSize >= 0;

                if (remainingSize == 0)
                    endReached = true;
            }

            if (endReached) {
                // Checking these helps a lot when catching corrupt
                // or truncated .lzma files. LZMA Utils doesn't do
                // the first check and thus it accepts many invalid
                // files that this implementation and XZ Utils don't.
                if (!lzma.getRc().isFinished() || lzma.getLz().hasPending())
                    throw new CorruptedInputException();

                putArraysToCache();
                return size == 0 ? -1 : size;
            }
        }

        return size;
    }

    private void putArraysToCache() {
        lzma.getLz().putArraysToCache();
    }

    /**
     * Closes the stream and calls <code>in.close()</code>.
     * If the stream was already closed, this does nothing.
     *
     * @throws IOException if thrown by <code>in.close()</code>
     */
    @Override
    public void close() throws IOException {
        lzma.close();
    }
}
