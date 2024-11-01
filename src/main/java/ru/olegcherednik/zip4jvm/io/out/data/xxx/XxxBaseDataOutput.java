package ru.olegcherednik.zip4jvm.io.out.data.xxx;

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 01.11.2024
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class XxxBaseDataOutput implements DataOutput {

    protected final DataOutput delegate;

    // --------------------

    @Override
    public long getRelativeOffs() {
        return delegate.getRelativeOffs();
    }

    @Override
    public void writeByte(int val) throws IOException {
        delegate.writeByte(val);
    }

    @Override
    public void writeWord(int val) throws IOException {
        delegate.writeWord(val);
    }

    @Override
    public void writeDword(long val) throws IOException {
        delegate.writeDword(val);
    }

    @Override
    public void writeQword(long val) throws IOException {
        delegate.writeQword(val);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        delegate.write(buf, offs, len);
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    // ---------- Closeable ----------

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    // ---------- Marker ----------

    @Override
    public void mark(String id) {
        delegate.mark(id);
    }

    @Override
    public long getMark(String id) {
        return delegate.getMark(id);
    }

    @Override
    public long getWrittenBytesAmount(String id) {
        return delegate.getWrittenBytesAmount(id);
    }
}
