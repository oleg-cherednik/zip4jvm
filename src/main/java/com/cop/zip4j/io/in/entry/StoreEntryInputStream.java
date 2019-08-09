package com.cop.zip4j.io.in.entry;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.ZipModel;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
final class StoreEntryInputStream extends EntryInputStream {

    private final ZipModel zipModel;
    private final Decoder decoder;
    private final long compressedSize;
    private final DataInput in;

    private int readBytes;

    public StoreEntryInputStream(ZipModel zipModel, LocalFileHeader localFileHeader, Decoder decoder, DataInput in) {
        super(localFileHeader);
        this.zipModel = zipModel;
        this.decoder = decoder;
        this.in = in;
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
            readBytes += len;
            updateChecksum(buf, offs, len);
        }

        return len;
    }

}
