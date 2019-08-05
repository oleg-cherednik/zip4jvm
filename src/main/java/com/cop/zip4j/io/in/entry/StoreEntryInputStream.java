package com.cop.zip4j.io.in.entry;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.io.in.MarkDataInput;
import com.cop.zip4j.model.ZipModel;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
final class StoreEntryInputStream extends InputStream {

    private static final String MARK = StoreEntryInputStream.class.getSimpleName();

    private final ZipModel zipModel;
    private final Decoder decoder;
    private final long size;
    private final MarkDataInput in;

    private long bytesRead;

    public StoreEntryInputStream(ZipModel zipModel, Decoder decoder, long size, MarkDataInput in) {
        this.zipModel = zipModel;
        this.decoder = decoder;
        this.size = size;
        this.in = in;

        in.mark(MARK);
    }

    @Override
    public int available() {
        return (int)Math.max(0, size - in.getWrittenBytesAmount(MARK));
    }

    @Override
    public int read() throws IOException {
        return available() == 0 ? IOUtils.EOF : in.readByte();
    }

}
