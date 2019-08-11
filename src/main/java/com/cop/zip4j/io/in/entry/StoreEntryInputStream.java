package com.cop.zip4j.io.in.entry;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.LocalFileHeader;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
final class StoreEntryInputStream extends EntryInputStream {

    private final DataInput in;
    private final Decoder decoder;
    private final long compressedSize;

    public StoreEntryInputStream(DataInput in, LocalFileHeader localFileHeader, Decoder decoder) {
        super(localFileHeader);
        this.in = in;
        this.decoder = decoder;
        compressedSize = decoder.getCompressedSize(localFileHeader);
    }

    @Override
    public int available() {
        return (int)Math.max(0, compressedSize - readBytes);
    }

    @Override
    public int read() throws IOException {
        if (available() == 0)
            return IOUtils.EOF;

        byte[] buf = new byte[1];
        int len = read(buf, 0, 1);

        if (len == IOUtils.EOF)
            return IOUtils.EOF;

        return buf[0] & 0xFF;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
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
    }

}
