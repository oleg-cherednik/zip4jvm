package ru.olegcherednik.zip4jvm.io.out.entry;

import ru.olegcherednik.zip4jvm.io.bzip2.Bzip2OutputStream;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 12.04.2020
 */
final class Bzip2EntryOutputStream extends EntryOutputStream {

    private final Bzip2OutputStream bzip2;

    public Bzip2EntryOutputStream(ZipEntry zipEntry, DataOutput out) throws IOException {
        super(zipEntry, out);
        bzip2 = createEncoder();
    }

    private Bzip2OutputStream createEncoder() throws IOException {
        return new Bzip2OutputStream(out, zipEntry.getCompressionLevel());
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        super.write(buf, offs, len);
        bzip2.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        bzip2.close();
        super.close();
    }

}
