package ru.olegcherednik.zip4jvm.io.in.entry;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.lzma.DecoderNew;
import ru.olegcherednik.zip4jvm.io.lzma.LzmaProperties;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 02.02.2020
 */
final class LzmaEntryInputStream extends EntryInputStream {

    private final DecoderNew dec;

    public LzmaEntryInputStream(ZipEntry zipEntry, DataInput in, Decoder decoder) throws IOException {
        super(zipEntry, in, decoder);

        byte[] buf = new byte[1024];
        in.read(buf, 0, buf.length);
        decoder.decrypt(buf, 0, buf.length);

        in.mark("aa");
        int majorVersion = in.readByte();
        int minorVersion = in.readByte();
        int headerSize = in.readWord();

        LzmaProperties properties = new LzmaProperties();
        properties.read(in);

        dec = new DecoderNew(in, properties, Long.MAX_VALUE);
        readCompressedBytes += in.getOffs() - in.getMark("aa");
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

    @Override
    public void close() throws IOException {
        super.close();
    }
}
