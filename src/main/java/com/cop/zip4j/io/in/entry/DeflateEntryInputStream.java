package com.cop.zip4j.io.in.entry;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.utils.InternalZipConstants;
import org.apache.commons.io.IOUtils;

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
final class DeflateEntryInputStream extends EntryInputStream {

    private byte[] oneByteBuff = new byte[1];

    private final DataInput in;
    private final long compressedSize;
    private final byte[] buf = new byte[InternalZipConstants.BUF_SIZE];
    private final Inflater inflater = new Inflater(true);

    private int readBytes;

    public DeflateEntryInputStream(LocalFileHeader localFileHeader, Decoder decoder, DataInput in) {
        super(localFileHeader);
        this.in = in;
        compressedSize = decoder.getCompressedSize(localFileHeader);
    }

    @Override
    public int available() {
        int len = (int)Math.max(0, compressedSize - readBytes);

        if (len == 0)
            return inflater.finished() ? 0 : 1;

        return len;
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
//    @Override
//    public int available() {
//        return inflater.finished() ? 0 : 1;
//    }
    @Override
    public int read() throws IOException {
        return read(oneByteBuff, 0, 1) == -1 ? -1 : oneByteBuff[0] & 0xff;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        try {
            if (readBytes >= compressedSize) {
                finishInflating();
                return IOUtils.EOF;
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

            return n;
        } catch(DataFormatException e) {
            String s = "Invalid ZLIB data format";
            if (e.getMessage() != null) {
                s = e.getMessage();
            }
            // TODO fix it, localFileHeader is not belong to UnzipEngine
//            if (unzipEngine != null)
//                if (unzipEngine.getLocalFileHeader().getEncryption() == Encryption.STANDARD)
//                    s += " - Wrong Password?";
            throw new IOException(s);
        }
    }

    private void finishInflating() throws IOException {
        //In some cases, complete data is not read even though inflater is complete
        //make sure to read complete data before returning -1
        byte[] b = new byte[1024];
        while (in.read(b, 0, 1024) != -1) {
            //read all data
        }
    }

    private void fill() throws IOException {
        int len = Math.min(buf.length, available());
        len = in.read(buf, 0, len);

        if (len == IOUtils.EOF)
            throw new EOFException("Unexpected end of ZLIB input stream");

        readBytes += len;

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

    @Override
    public void close() throws IOException {
        inflater.end();
        in.close();
    }
}
