package ru.olegcherednik.zip4jvm.io.in.buf;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.bzip2.Bzip2InputStream;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 20.12.2022
 */
public class Bzip2DataInput extends ByteArrayDataInput {

    public Bzip2DataInput(DataInput in, int uncompressedSize) {
        super(read(in, uncompressedSize), in.getEndianness());
    }

    private static byte[] read(DataInput in, int uncompressedSize) {
        try (Bzip2InputStream bzip = new Bzip2InputStream(in)) {
            byte[] buf = new byte[uncompressedSize];
            bzip.read(buf, 0, buf.length);
            return buf;
        } catch(IOException e) {
            throw new Zip4jvmException(e);
        }
    }
}
