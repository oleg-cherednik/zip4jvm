package ru.olegcherednik.zip4jvm.io.out.entry;

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.io.zstd.ZstdOutputStream;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 07.11.2021
 */
final class ZstdEntryOutputStream extends EntryOutputStream {

    private final ZstdOutputStream zstd;

    public ZstdEntryOutputStream(ZipEntry zipEntry, DataOutput out) throws IOException {
        super(zipEntry, out);
        zstd = createEncoder();
    }

    private ZstdOutputStream createEncoder() throws IOException {
        return new ZstdOutputStream(out, zipEntry.getCompressionLevel());
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        super.write(buf, offs, len);
        zstd.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        zstd.close();
        super.close();
    }

}
