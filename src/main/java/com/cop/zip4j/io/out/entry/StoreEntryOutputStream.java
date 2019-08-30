package com.cop.zip4j.io.out.entry;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.entry.PathZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
final class StoreEntryOutputStream extends EntryOutputStream {

    public StoreEntryOutputStream(PathZipEntry entry, DataOutput out) {
        super(entry, out);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        updateChecksum(buf, offs, len);
        encoder.encrypt(buf, offs, len);
        out.write(buf, offs, len);
    }
}
