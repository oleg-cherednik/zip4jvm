package ru.olegcherednik.zip4jvm.io.in.buf;

import ru.olegcherednik.zip4jvm.io.ed.Deflate64CompressorInputStream;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputNew;

/**
 * @author Oleg Cherednik
 * @since 20.12.2022
 */
public class EnhancedDeflateDataInput extends ByteArrayDataInputNew {

    private final DataInputNew in;

    public EnhancedDeflateDataInput(DataInputNew in, int uncompressedSize) {
        super(Deflate64CompressorInputStream.read(in, uncompressedSize));
        this.in = in;
    }

    @Override
    public long toLong(byte[] buf, int offs, int len) {
        return in.toLong(buf, offs, len);
    }
}
