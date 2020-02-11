package ru.olegcherednik.zip4jvm.io.out.entry;

import ru.olegcherednik.zip4jvm.io.lzma.LzmaProperties;
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

    // https://sevenzip.osdn.jp/chm/cmdline/switches/method.htm
    // https://askubuntu.com/questions/491223/7z-ultra-settings-for-zip-format
    private final LZMAOutputStream lzma;

    public LzmaEntryOutputStream(ZipEntry zipEntry, DataOutput out) throws IOException {
        super(zipEntry, out);
        lzma = createEncoder();

    }

    private LZMAOutputStream createEncoder() throws IOException {
//        lzma = new LzmaEncoder();
//        lzma.SetAlgorithm(1);

//        lzma.SetDictionarySize(1 << 23);
//        lzma.SetNumFastBytes(128);
//        lzma.SetMatchFinder(1);
//        lzma.SetLcLpPb(3, 0, 2);
//        lzma.SetEndMarkerMode(zipEntry.isLzmaEosMarker());

        LzmaProperties properties = LzmaProperties.builder().lc(3).lp(0).pb(2).dictionarySize(1 << 23).build();

//        out.writeByte((byte)18);    // major version
//        out.writeByte((byte)5);     // minor version
//        out.writeWord(5);           // header size
//        properties.write(out);

        LZMA2Options options = new LZMA2Options();
        return new LZMAOutputStream(out, options, -1);
    }

    private boolean writeHeader = true;

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        super.write(buf, offs, len);

        if (writeHeader) {
            out.writeByte((byte)18);    // major version
            out.writeByte((byte)5);     // minor version
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
