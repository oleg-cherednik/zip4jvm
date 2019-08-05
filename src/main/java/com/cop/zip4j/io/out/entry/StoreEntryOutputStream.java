package com.cop.zip4j.io.out.entry;

import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.io.out.MarkDataOutput;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.ZipModel;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
final class StoreEntryOutputStream extends EntryOutputStream {

    public StoreEntryOutputStream(ZipModel zipModel, CentralDirectory.FileHeader fileHeader, Encoder encoder, MarkDataOutput out) {
        super(zipModel, fileHeader, encoder, out);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        super.write(buf, offs, len);
        encoder._write(buf, offs, len, out);
    }
}
