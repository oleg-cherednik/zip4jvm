package ru.olegcherednik.zip4jvm.io.in.entry;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
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

    private static final String HEADER = LzmaEntryInputStream.class.getSimpleName() + ".header";

    private final LzmaDecoder lzma;

    public LzmaEntryInputStream(ZipEntry zipEntry, DataInput in) throws IOException {
        super(zipEntry, in);
        lzma = createDecoder();
    }

    private LzmaDecoder createDecoder() throws IOException {
        in.mark(HEADER);
        in.readByte();    // major version
        in.readByte();    // minor version
        int headerSize = in.readWord();    // header size

        if (headerSize != 5)
            throw new Zip4jvmException(String.format("LZMA header size expected 5 bytes: actual is %d bytes", headerSize));

        long size = zipEntry.isLzmaEosMarker() ? Long.MAX_VALUE : zipEntry.getUncompressedSize();
        LzmaDecoder dec = new LzmaDecoder(in, LzmaProperties.read(in), size);
        readCompressedBytes += in.getOffs() - in.getMark(HEADER);
        return dec;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        len = lzma.decode(buf, offs, len);

        if (len == 0 || len == IOUtils.EOF)
            return IOUtils.EOF;

        readCompressedBytes += len;
        writtenUncompressedBytes += len;
        updateChecksum(buf, offs, len);
        return len;
    }
}
