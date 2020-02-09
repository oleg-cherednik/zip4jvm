package ru.olegcherednik.zip4jvm.io.in.entry;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.lzma.LzmaDecoder;
import ru.olegcherednik.zip4jvm.io.lzma.LzmaProperties;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 02.02.2020
 */
final class LzmaEntryInputStream extends EntryInputStream {

    private final LzmaDecoder dec;

    public LzmaEntryInputStream(ZipEntry zipEntry, DataInput in) throws IOException {
        super(zipEntry, in);
        dec = createDecoder();
    }

    private LzmaDecoder createDecoder() throws IOException {
        in.mark("aa");
        int majorVersion = in.readByte();
        int minorVersion = in.readByte();
        int headerSize = in.readWord();
        long size = zipEntry.isLzmaEosMarker() ? Long.MAX_VALUE : zipEntry.getUncompressedSize();

        LzmaDecoder dec = new LzmaDecoder(in, LzmaProperties.read(in), size);
        readCompressedBytes += in.getOffs() - in.getMark("aa");
        return dec;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        len = dec.decode(buf, offs, len);

        if (len == 0 || len == IOUtils.EOF)
            return IOUtils.EOF;

        readCompressedBytes += len;
        writtenUncompressedBytes += len;
        updateChecksum(buf, offs, len);
        return len;
    }
}
