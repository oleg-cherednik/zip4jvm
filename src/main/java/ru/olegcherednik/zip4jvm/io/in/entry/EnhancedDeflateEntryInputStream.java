package ru.olegcherednik.zip4jvm.io.in.entry;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.io.ed.Deflate64CompressorInputStream;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 15.04.2020
 */
final class EnhancedDeflateEntryInputStream extends EntryInputStream {

    private final Deflate64CompressorInputStream ed;

    public EnhancedDeflateEntryInputStream(ZipEntry zipEntry, DataInput in) throws IOException {
        super(zipEntry, in);
        ed = createInputStream();
    }

    private Deflate64CompressorInputStream createInputStream() throws IOException {
        return new Deflate64CompressorInputStream(in);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        len = ed.read(buf, offs, len);

        if (len == 0 || len == IOUtils.EOF)
            return IOUtils.EOF;

        readCompressedBytes += len;
        writtenUncompressedBytes += len;
        updateChecksum(buf, offs, len);
        return len;
    }
}
