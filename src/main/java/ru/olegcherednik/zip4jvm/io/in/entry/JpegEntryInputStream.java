package ru.olegcherednik.zip4jvm.io.in.entry;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.jpeg.JpegInputStream;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.09.2020
 */
final class JpegEntryInputStream extends EntryInputStream {

    private static final String HEADER = JpegEntryInputStream.class.getSimpleName() + ".header";

    private final JpegInputStream jpeg;

    public JpegEntryInputStream(ZipEntry zipEntry, DataInput in) throws IOException {
        super(zipEntry, in);
        jpeg = createInputStream();
    }

    private JpegInputStream createInputStream() throws IOException {
        return new JpegInputStream(in);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        len = jpeg.read(buf, offs, len);

        if (len == 0 || len == IOUtils.EOF)
            return IOUtils.EOF;

        readCompressedBytes += len;
        writtenUncompressedBytes += len;
        updateChecksum(buf, offs, len);

        return len;
    }
}
