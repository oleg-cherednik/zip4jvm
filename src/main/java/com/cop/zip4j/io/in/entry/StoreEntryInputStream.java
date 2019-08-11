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

    public StoreEntryInputStream(DataInput in, LocalFileHeader localFileHeader, Decoder decoder) {
        super(in, localFileHeader, decoder);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        len = (int)Math.min(len, getAvailableCompressedBytes());
        len = decoder.getLen(readCompressedBytes, len, compressedSize);

        if(len == 0)
            return IOUtils.EOF;

        int bytes = _read(buf, offs, len);

        if (bytes != IOUtils.EOF) {
            readCompressedBytes += bytes;
            writtenUncompressedBytes += bytes;
            updateChecksum(buf, offs, bytes);
        }

        return bytes;
    }

}
