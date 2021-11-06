package ru.olegcherednik.zip4jvm.io.in.entry;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.zstd.ZstdInputStream;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 06.11.2021
 */
final class ZstdEntryInputStream extends EntryInputStream {

    private final ZstdInputStream zstd;

    public ZstdEntryInputStream(ZipEntry zipEntry, DataInput in) throws IOException {
        super(zipEntry, in);
        zstd = createInputStream();
    }

    private ZstdInputStream createInputStream() throws IOException {
        return new ZstdInputStream(in);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        len = zstd.read(buf, offs, len);

        if (len == 0 || len == IOUtils.EOF)
            return IOUtils.EOF;

        readCompressedBytes += len;
        writtenUncompressedBytes += len;
        updateChecksum(buf, offs, len);
        return len;
    }

}
