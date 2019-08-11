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
final class InflateEntryInputStream extends EntryInputStream {

    private final byte[] buf = new byte[InternalZipConstants.BUF_SIZE];
    private final Inflater inflater = new Inflater(true);

    private long writtenUncompressedBytes;

    public InflateEntryInputStream(DataInput in, LocalFileHeader localFileHeader, Decoder decoder) {
        super(in, localFileHeader, decoder);
    }

    @Override
    public int available() {
        int bytes = (int)Math.max(0, uncompressedSize - writtenUncompressedBytes);

        if (bytes == 0)
            return inflater.finished() ? 0 : 1;

        return bytes;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        try {
            if (writtenUncompressedBytes >= uncompressedSize)
                return IOUtils.EOF;

            int n;

            while ((n = inflater.inflate(buf, offs, len)) == 0) {
                if (inflater.finished() || inflater.needsDictionary())
                    return IOUtils.EOF;

                if (inflater.needsInput())
                    fill();
            }

            writtenUncompressedBytes += n;
            return n;
        } catch(DataFormatException e) {
            throw new IOException(e);
        }
    }

    private void fill() throws IOException {
        int len = _read(buf, 0, buf.length);

        if (len == IOUtils.EOF)
            throw new EOFException("Unexpected end of ZLIB input stream");

        inflater.setInput(buf, 0, len);
    }

    private long getAvailableCompressedBytes() {
        return Math.max(0, compressedSize - readBytes);
    }

    private int _read(byte[] buf, int offs, int len) throws IOException {
        len = (int)Math.min(len, getAvailableCompressedBytes());

        if (len == 0)
            return IOUtils.EOF;

        len = in.read(buf, offs, decoder.getLen(readBytes, len, compressedSize));

        if (len != IOUtils.EOF) {
            decoder.decrypt(buf, offs, len);
            updateChecksum(buf, offs, len);
            readBytes += len;
        }

        return len;
    }

    /*
            len = Math.min(len, available());

        if (len == 0)
            return IOUtils.EOF;

        len = in.read(buf, offs, len);

        if (len != IOUtils.EOF) {
            decoder.decrypt(buf, offs, len);
            updateChecksum(buf, offs, len);
            readBytes += len;
        }

        return len;
     */

    @Override
    public void close() throws IOException {
        inflater.end();
        in.close();
    }

}
