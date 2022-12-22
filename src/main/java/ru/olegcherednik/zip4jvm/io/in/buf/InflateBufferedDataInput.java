package ru.olegcherednik.zip4jvm.io.in.buf;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.bzip2.Bzip2InputStream;
import ru.olegcherednik.zip4jvm.io.in.data.BaseDataInputNew;
import ru.olegcherednik.zip4jvm.io.in.data.CommonBaseDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputNew;

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * @author Oleg Cherednik
 * @since 18.12.2022
 */
public class InflateBufferedDataInput extends ByteArrayDataInputNew {

    private final DataInputNew in;

    public InflateBufferedDataInput(DataInputNew in, int uncompressedSize) {
        super(read(in, uncompressedSize));
        this.in = in;
    }

    private static byte[] read(DataInputNew in, int uncompressedSize) {
        try {
            Inflater inflater = new Inflater(true);
            inflater.setInput(in.readBytes((int)in.size()));

            byte[] buf = new byte[uncompressedSize];
            inflater.inflate(buf, 0, buf.length);
            return buf;
        } catch(IOException | DataFormatException e) {
            throw new Zip4jvmException(e);
        }
    }

    @Override
    public long toLong(byte[] buf, int offs, int len) {
        return in.toLong(buf, offs, len);
    }
}
