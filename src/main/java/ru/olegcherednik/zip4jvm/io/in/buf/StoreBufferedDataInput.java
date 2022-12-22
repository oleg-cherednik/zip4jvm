package ru.olegcherednik.zip4jvm.io.in.buf;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputNew;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 18.12.2022
 */
public class StoreBufferedDataInput extends ByteArrayDataInputNew {

    public StoreBufferedDataInput(DataInputNew in, int uncompressedSize) {
        super(read(in, uncompressedSize), in.getEndianness());
    }

    private static byte[] read(DataInputNew in, int uncompressedSize) {
        try {
            return in.readBytes(uncompressedSize);
        } catch(IOException e) {
            throw new Zip4jvmException(e);
        }
    }

}
