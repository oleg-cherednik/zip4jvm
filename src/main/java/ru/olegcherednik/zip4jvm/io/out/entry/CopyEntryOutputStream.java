package ru.olegcherednik.zip4jvm.io.out.entry;

import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 14.09.2019
 */
final class CopyEntryOutputStream extends EntryOutputStream {

    public CopyEntryOutputStream(ZipEntry entry, DataOutput out) {
        super(entry, out);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        updateChecksum(buf, offs, len);
        encoder.encrypt(buf, offs, len);
        out.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
    }
}
