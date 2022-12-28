package ru.olegcherednik.zip4jvm.io.in.buf;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputLocation;

/**
 * @author Oleg Cherednik
 * @since 18.12.2022
 */
public class StoreDataInput extends MetadataByteArrayDataInput {

    public StoreDataInput(DataInput in, int uncompressedSize, DataInputLocation dataInputLocation) {
        super(read(in, uncompressedSize), in.getEndianness(), dataInputLocation);
    }

    private static byte[] read(DataInput in, int uncompressedSize) {
        return in.readBytes(uncompressedSize);
    }

}
