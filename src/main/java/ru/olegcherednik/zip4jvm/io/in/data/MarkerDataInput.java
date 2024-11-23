package ru.olegcherednik.zip4jvm.io.in.data;

import ru.olegcherednik.zip4jvm.utils.ThreadLocalBuffer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 23.11.2024
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class MarkerDataInput implements DataInput {

    protected final DataInput in;

    // ---------- Marker ----------

    @Override
    public void mark(String id) {
        in.mark(id);
    }

    @Override
    public long getMark(String id) {
        return in.getMark(id);
    }

    @Override
    public long getMarkSize(String id) {
        return in.getMarkSize(id);
    }

    // ---------- ReadBuffer ----------

    @Override
    public int read() throws IOException {
        byte[] buf = ThreadLocalBuffer.getOne();
        int b = read(buf, 0, buf.length);
        return b == IOUtils.EOF ? IOUtils.EOF : buf[0] & 0xFF;
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        in.close();
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return in.toString();
    }

}
