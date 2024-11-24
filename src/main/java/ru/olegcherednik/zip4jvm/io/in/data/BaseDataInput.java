package ru.olegcherednik.zip4jvm.io.in.data;

import ru.olegcherednik.zip4jvm.utils.ThreadLocalBuffer;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 24.11.2024
 */
public abstract class BaseDataInput implements DataInput {

    // ---------- ReadBuffer ----------

    @Override
    public final int read() throws IOException {
        byte[] buf = ThreadLocalBuffer.getOne();
        read(buf, 0, buf.length);
        return buf[0] & 0xFF;
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
    }

}
