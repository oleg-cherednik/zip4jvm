package ru.olegcherednik.zip4jvm.io.in.buf;

import ru.olegcherednik.zip4jvm.io.ed.Deflate64CompressorInputStream;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputNew;

/**
 * @author Oleg Cherednik
 * @since 20.12.2022
 */
public class EnhancedDeflateDataInputNew extends ByteArrayDataInputNew {

    public EnhancedDeflateDataInputNew(DataInputNew in, int uncompressedSize) {
        super(Deflate64CompressorInputStream.read(in, uncompressedSize), in.getEndianness());
    }

}
