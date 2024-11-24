package ru.olegcherednik.zip4jvm.io.in;

import ru.olegcherednik.zip4jvm.utils.ThreadLocalBuffer;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 24.11.2024
 */
public abstract class BaseDataInput implements DataInput {

    // ---------- DataInput ----------

    @Override
    public int readByte() throws IOException {
        return getByteOrder().readByte(this);
    }

    @Override
    public int readWord() throws IOException {
        return getByteOrder().readWord(this);
    }

    @Override
    public long readDword() throws IOException {
        return getByteOrder().readDword(this);
    }

    @Override
    public long readQword() throws IOException {
        return getByteOrder().readQword(this);
    }

    // ---------- ReadBuffer ----------

    @Override
    public final int read() throws IOException {
        byte[] buf = ThreadLocalBuffer.getOne();
        read(buf, 0, buf.length);
        return buf[0] & 0xFF;
    }

}
