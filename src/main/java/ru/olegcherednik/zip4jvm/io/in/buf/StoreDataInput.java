package ru.olegcherednik.zip4jvm.io.in.buf;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

/**
 * @author Oleg Cherednik
 * @since 18.12.2022
 */
public class StoreDataInput extends ByteArrayDataInput {

    public StoreDataInput(DataInput in, int uncompressedSize) {
        super(read(in, uncompressedSize), in.getEndianness());
    }

    private static byte[] read(DataInput in, int uncompressedSize) {
        return in.readBytes(uncompressedSize);
    }

}
