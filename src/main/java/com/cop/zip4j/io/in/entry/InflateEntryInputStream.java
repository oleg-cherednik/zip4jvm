package com.cop.zip4j.io.in.entry;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.utils.InternalZipConstants;

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
final class InflateEntryInputStream extends EntryInputStream {

    private final byte[] buf = new byte[InternalZipConstants.BUF_SIZE];
    private final Inflater inflater = new Inflater(true);

    private final DataInput in;
    private final Decoder decoder;
    private final long compressedSize;

    private byte[] oneByteBuff = new byte[1];
    private long bytesWritten;

    public InflateEntryInputStream(DataInput in, LocalFileHeader localFileHeader, Decoder decoder) {
        super(localFileHeader);
        this.in = in;
        this.decoder = decoder;
        compressedSize = decoder.getCompressedSize(localFileHeader);
    }

    @Override
    public int read() throws IOException {
        return read(oneByteBuff, 0, 1) == -1 ? -1 : oneByteBuff[0] & 0xff;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        try {
            if (bytesWritten >= localFileHeader.getUncompressedSize()) {
                finishInflating();
                return -1;
            }

            int n;

            while ((n = inflater.inflate(buf, offs, len)) == 0) {
                if (inflater.finished() || inflater.needsDictionary()) {
                    finishInflating();
                    return -1;
                }

                if (inflater.needsInput())
                    fill();
            }

            bytesWritten += n;
            return n;
        } catch(DataFormatException e) {
            throw new IOException(e);
        }
    }

    private void finishInflating() throws IOException {
        //In some cases, compelte data is not read even though inflater is complete
        //make sure to read complete data before returning -1
        byte[] b = new byte[1024];
        while (_read(b, 0, 1024) != -1) {
            //read all data
        }
    }

    private void fill() throws IOException {
        int len = _read(buf, 0, buf.length);

        if (len == -1)
            throw new EOFException("Unexpected end of ZLIB input stream");

        inflater.setInput(buf, 0, len);
    }

    /**
     * Skips specified number of bytes of uncompressed data.
     *
     * @param n the number of bytes to skip
     * @return the actual number of bytes skipped.
     * @throws IOException              if an I/O error has occurred
     * @throws IllegalArgumentException if n < 0
     */
    @Override
    public long skip(long n) throws IOException {
        if (n < 0)
            throw new IllegalArgumentException("negative skip length");

        int max = (int)Math.min(n, Integer.MAX_VALUE);
        int total = 0;
        byte[] b = new byte[512];

        while (total < max) {
            int len = max - total;

            if (len > b.length)
                len = b.length;

            len = read(b, 0, len);

            if (len == -1)
                break;

            total += len;
        }
        return total;
    }

    /**
     * Returns 0 after EOF has been reached, otherwise always return 1.
     * <p>
     * Programs should not count on this method to return the actual number
     * of bytes that could be read without blocking.
     *
     * @return 1 before EOF and 0 after EOF.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int available() {
        return inflater.finished() ? 0 : 1;
    }

    @Override
    public void close() throws IOException {
        inflater.end();
        in.close();
    }


    // -------------------

    private long bytesRead;

    private int _read(byte[] buf, int offs, int len) throws IOException {
        if (len > compressedSize - bytesRead) {
            len = (int)(compressedSize - bytesRead);

            if (len == 0)
                return -1;
        }

        len = decoder.getLen(bytesRead, len, compressedSize);


        int count = in.read(buf, offs, len);

        if (count > 0) {
            decoder.decrypt(buf, offs, count);
            bytesRead += count;
        }

        return count;
    }
}
