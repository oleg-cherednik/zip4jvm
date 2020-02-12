package ru.olegcherednik.zip4jvm.io.out.entry;

import ru.olegcherednik.zip4jvm.io.lzma.xz.LZMA2Options;
import ru.olegcherednik.zip4jvm.io.lzma.xz.LZMAOutputStream;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 09.02.2020
 */
final class LzmaEntryOutputStream extends EntryOutputStream {

    private final LZMAOutputStream lzma;
    private boolean writeHeader = true;

    public LzmaEntryOutputStream(ZipEntry zipEntry, DataOutput out) throws IOException {
        super(zipEntry, out);
        lzma = createEncoder();
    }

    private LZMAOutputStream createEncoder() throws IOException {
        long size = zipEntry.isLzmaEosMarker() ? -1 : zipEntry.getUncompressedSize();
        int compressionLevel = zipEntry.getCompressionLevel().getCode();
        return new LZMAOutputStream(out, new LZMA2Options(compressionLevel), size);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        super.write(buf, offs, len);

        if (writeHeader) {
            out.writeByte((byte)19);    // major version
            out.writeByte((byte)0);     // minor version
            out.writeWord(5);           // header size
            lzma.writeHeader();
            writeHeader = false;
        }

        lzma.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        lzma.finish();
        super.close();
    }

}
