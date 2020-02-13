/*
 * LZMAInputStream
 *
 * Authors: Lasse Collin <lasse.collin@tukaani.org>
 *          Igor Pavlov <http://7-zip.org/>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package ru.olegcherednik.zip4jvm.io.lzma.xz;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.lzma.xz.exceptions.CorruptedInputException;
import ru.olegcherednik.zip4jvm.io.lzma.xz.exceptions.MemoryLimitException;
import ru.olegcherednik.zip4jvm.io.lzma.xz.exceptions.UnsupportedOptionsException;
import ru.olegcherednik.zip4jvm.io.lzma.xz.lz.LZDecoder;
import ru.olegcherednik.zip4jvm.io.lzma.xz.lzma.LZMADecoder;
import ru.olegcherednik.zip4jvm.io.lzma.xz.rangecoder.RangeDecoderFromStream;

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
 * Even when using <code>BufferedInputStream</code>, the performance tends
 * to be worse (maybe 10-20&nbsp;% slower) than with {@link LZMA2InputStream}
 * or {@link XZInputStream} (when the .xz file contains LZMA2-compressed data).
 *
 * @since 1.4
 */
public class LzmaInputStream extends InputStream {

    /**
     * Largest dictionary size supported by this implementation.
     * <p>
     * LZMA allows dictionaries up to one byte less than 4 GiB. This
     * implementation supports only 16 bytes less than 2 GiB. This
     * limitation is due to Java using signed 32-bit integers for array
     * indexing. The limitation shouldn't matter much in practice since so
     * huge dictionaries are not normally used.
     */
    public static final int DICT_SIZE_MAX = Integer.MAX_VALUE & ~15;

    private DataInput in;
    private final ArrayCache arrayCache = ArrayCache.getDefaultCache();
    private LZDecoder lz;
    private RangeDecoderFromStream rc;
    private LZMADecoder lzma;

    private boolean endReached = false;

    private final byte[] tempBuf = new byte[1];

    /**
     * Number of uncompressed bytes left to be decompressed, or -1 if
     * the end marker is used.
     */
    private long remainingSize;

    private IOException exception = null;

    /**
     * Gets approximate decompressor memory requirements as kibibytes for
     * the given dictionary size and LZMA properties byte (lc, lp, and pb).
     *
     * @param dictSize  LZMA dictionary size as bytes, should be
     *                  in the range [<code>0</code>,
     *                  <code>DICT_SIZE_MAX</code>]
     * @param propsByte LZMA properties byte that encodes the values
     *                  of lc, lp, and pb
     * @return approximate memory requirements as kibibytes (KiB)
     * @throws UnsupportedOptionsException if <code>dictSize</code> is outside
     *                                     the range [<code>0</code>,
     *                                     <code>DICT_SIZE_MAX</code>]
     * @throws CorruptedInputException     if <code>propsByte</code> is invalid
     */
    public static int getMemoryUsage(int dictSize, int propsByte)
            throws UnsupportedOptionsException, CorruptedInputException {
        if (dictSize < 0 || dictSize > DICT_SIZE_MAX)
            throw new UnsupportedOptionsException(
                    "LZMA dictionary is too big for this implementation");

        int props = propsByte & 0xFF;
        if (props > (4 * 5 + 4) * 9 + 8)
            throw new CorruptedInputException("Invalid LZMA properties byte");

        props %= 9 * 5;
        int lp = props / 9;
        int lc = props - lp * 9;

        return getMemoryUsage(dictSize, lc, lp);
    }

    /**
     * Gets approximate decompressor memory requirements as kibibytes for
     * the given dictionary size, lc, and lp. Note that pb isn't needed.
     *
     * @param dictSize LZMA dictionary size as bytes, must be
     *                 in the range [<code>0</code>,
     *                 <code>DICT_SIZE_MAX</code>]
     * @param lc       number of literal context bits, must be
     *                 in the range [0, 8]
     * @param lp       number of literal position bits, must be
     *                 in the range [0, 4]
     * @return approximate memory requirements as kibibytes (KiB)
     */
    public static int getMemoryUsage(int dictSize, int lc, int lp) {
        if (lc < 0 || lc > 8 || lp < 0 || lp > 4)
            throw new IllegalArgumentException("Invalid lc or lp");

        // Probability variables have the type "short". There are
        // 0x300 (768) probability variables in each literal subcoder.
        // The number of literal subcoders is 2^(lc + lp).
        //
        // Roughly 10 KiB for the base state + LZ decoder's dictionary buffer
        // + sizeof(short) * number probability variables per literal subcoder
        //   * number of literal subcoders
        return 10 + getDictSize(dictSize) / 1024
                + ((2 * 0x300) << (lc + lp)) / 1024;
    }

    private static int getDictSize(int dictSize) {
        if (dictSize < 0 || dictSize > DICT_SIZE_MAX)
            throw new IllegalArgumentException(
                    "LZMA dictionary is too big for this implementation");

        // For performance reasons, use a 4 KiB dictionary if something
        // smaller was requested. It's a rare situation and the performance
        // difference isn't huge, and it starts to matter mostly when the
        // dictionary is just a few bytes. But we need to handle the special
        // case of dictSize == 0 anyway, which is an allowed value but in
        // practice means one-byte dictionary.
        //
        // Note that using a dictionary bigger than specified in the headers
        // can hide errors if there is a reference to data beyond the original
        // dictionary size but is still within 4 KiB.
        if (dictSize < 4096)
            dictSize = 4096;

        // Round dictionary size upward to a multiple of 16. This way LZMA
        // can use LZDecoder.getPos() for calculating LZMA's posMask.
        return (dictSize + 15) & ~15;
    }

    /**
     * Creates a new .lzma file format decompressor with an optional
     * memory usage limit.
     * <p>
     * This is identical to <code>LZMAInputStream(InputStream, int)</code>
     * except that this also takes the <code>arrayCache</code> argument.
     *
     * @param in          input stream from which .lzma data is read;
     *                    it might be a good idea to wrap it in
     *                    <code>BufferedInputStream</code>, see the
     *                    note at the top of this page
     * @param uncompSize uncompressed size or <t>-1</t> if unknown
     * @throws CorruptedInputException     file is corrupt or perhaps not in
     *                                     the .lzma format at all
     * @throws UnsupportedOptionsException dictionary size or uncompressed size is too
     *                                     big for this implementation
     * @throws MemoryLimitException        memory usage limit was exceeded
     * @throws IOException                 may be thrown by <code>in</code>
     * @since 1.7
     */
    public LzmaInputStream(DataInput in, long uncompSize) throws IOException {
        int propsByte = in.readByte();
        int dictSize = (int)in.readDword();
        initialize(in, uncompSize, propsByte, dictSize, null);
    }

    private void initialize(DataInput in, long uncompSize, int propsByte,
            int dictSize, byte[] presetDict)
            throws IOException {
        // Validate the uncompressed size since the other "initialize" throws
        // IllegalArgumentException if uncompSize < -1.
        if (uncompSize < -1)
            throw new UnsupportedOptionsException(
                    "Uncompressed size is too big");

        // Decode the properties byte. In contrast to LZMA2, there is no
        // limit of lc + lp <= 4.
        int props = propsByte & 0xFF;
        if (props > (4 * 5 + 4) * 9 + 8)
            throw new CorruptedInputException("Invalid LZMA properties byte");

        int pb = props / (9 * 5);
        props -= pb * 9 * 5;
        int lp = props / 9;
        int lc = props - lp * 9;

        // Validate the dictionary size since the other "initialize" throws
        // IllegalArgumentException if dictSize is not supported.
        if (dictSize < 0 || dictSize > DICT_SIZE_MAX)
            throw new UnsupportedOptionsException(
                    "LZMA dictionary is too big for this implementation");

        initialize(in, uncompSize, lc, lp, pb, dictSize, presetDict);
    }

    private void initialize(DataInput in, long uncompSize,
            int lc, int lp, int pb,
            int dictSize, byte[] presetDict)
            throws IOException {
        // getDictSize validates dictSize and gives a message in
        // the exception too, so skip validating dictSize here.
        if (uncompSize < -1 || lc < 0 || lc > 8 || lp < 0 || lp > 4
                || pb < 0 || pb > 4)
            throw new IllegalArgumentException();

        this.in = in;

        // If uncompressed size is known, use it to avoid wasting memory for
        // a uselessly large dictionary buffer.
        dictSize = getDictSize(dictSize);
        if (uncompSize >= 0 && dictSize > uncompSize)
            dictSize = getDictSize((int)uncompSize);

        lz = new LZDecoder(getDictSize(dictSize), presetDict, arrayCache);
        rc = new RangeDecoderFromStream(in);
        lzma = new LZMADecoder(lz, rc, lc, lp, pb);

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

        if (in == null)
            throw new XZIOException("Stream closed");

        if (exception != null)
            throw exception;

        if (endReached)
            return -1;

        try {
            int size = 0;

            while (len > 0) {
                // If uncompressed size is known and thus no end marker will
                // be present, set the limit so that the uncompressed size
                // won't be exceeded.
                int copySizeMax = len;
                if (remainingSize >= 0 && remainingSize < len)
                    copySizeMax = (int)remainingSize;

                lz.setLimit(copySizeMax);

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
                    rc.normalize();
                }

                // Copy from the dictionary to buf.
                int copiedSize = lz.flush(buf, off);
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
                    if (!rc.isFinished() || lz.hasPending())
                        throw new CorruptedInputException();

                    putArraysToCache();
                    return size == 0 ? -1 : size;
                }
            }

            return size;

        } catch(IOException e) {
            exception = e;
            throw e;
        }
    }

    private void putArraysToCache() {
        if (lz != null) {
            lz.putArraysToCache(arrayCache);
            lz = null;
        }
    }

    /**
     * Closes the stream and calls <code>in.close()</code>.
     * If the stream was already closed, this does nothing.
     *
     * @throws IOException if thrown by <code>in.close()</code>
     */
    public void close() throws IOException {
        if (in != null) {
            putArraysToCache();

            try {
                in.close();
            } finally {
                in = null;
            }
        }
    }
}
