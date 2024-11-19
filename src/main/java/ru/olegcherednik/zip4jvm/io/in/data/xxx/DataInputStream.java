package ru.olegcherednik.zip4jvm.io.in.data.xxx;

import ru.olegcherednik.zip4jvm.utils.ThreadLocalBuffer;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 19.11.2024
 */
@RequiredArgsConstructor
public class DataInputStream extends InputStream {

    private final RandomAccessDataInput in;

    public static DataInputStream create(RandomAccessDataInput in) {
        return new DataInputStream(in);
    }

    // ---------- InputStream ----------

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return in.read(buf, offs, len);
    }

    @Override
    public final int read() throws IOException {
        byte[] buf = ThreadLocalBuffer.getOne();
        read(buf, 0, buf.length);
        return buf[0] & 0xFF;
    }

}
