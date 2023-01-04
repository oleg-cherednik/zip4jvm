package ru.olegcherednik.zip4jvm.io.in.buf;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputLocation;

import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * @author Oleg Cherednik
 * @since 18.12.2022
 */
public class InflateDataInput extends MetadataByteArrayDataInput {

    public InflateDataInput(DataInput in, int uncompressedSize, DataInputLocation dataInputLocation) {
        super(read(in, uncompressedSize), in.getEndianness(), dataInputLocation);
    }

    private static byte[] read(DataInput in, int uncompressedSize) {
        try {
            Inflater inflater = new Inflater(true);
            inflater.setInput(in.readBytes((int)in.size()));

            byte[] buf = new byte[uncompressedSize];
            inflater.inflate(buf, 0, buf.length);
            return buf;
        } catch(DataFormatException e) {
            throw new Zip4jvmException(e);
        }
    }
}
