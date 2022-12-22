package ru.olegcherednik.zip4jvm.io.in.buf;

import ru.olegcherednik.zip4jvm.io.bzip2.Bzip2InputStream;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputNew;

/**
 * @author Oleg Cherednik
 * @since 20.12.2022
 */
public class Bzip2DataInputNew extends ByteArrayDataInputNew {

    private final DataInputNew in;

    public Bzip2DataInputNew(DataInputNew in, int uncompressedSize) {
        super(Bzip2InputStream.read(in, uncompressedSize));
        this.in = in;
    }

    @Override
    public long toLong(byte[] buf, int offs, int len) {
        return in.toLong(buf, offs, len);
    }

}
