package com.cop.zip4j.io.entry;

import com.cop.zip4j.io.out.MarkDataOutput;
import com.cop.zip4j.model.ZipModel;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
final class StoreEntryOutputStream extends EntryOutputStream {

    public StoreEntryOutputStream(ZipModel zipModel, MarkDataOutput out) {
        super(zipModel, out);
    }

    @Override
    protected void writeImpl(byte[] buf, int offs, int len) throws IOException {
        encoder._write(buf, offs, len, out);
    }
}
