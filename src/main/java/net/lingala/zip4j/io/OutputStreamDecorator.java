package net.lingala.zip4j.io;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.lingala.zip4j.util.Raw;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 12.03.2019
 */
@Getter
@Setter
@RequiredArgsConstructor
public final class OutputStreamDecorator implements Closeable {

    private final byte[] intByte = new byte[4];

    private final OutputStream delegate;
    // TODO temporary public
    public long offs;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private long mark;

    public void mark() {
        mark = offs;
    }

    public long getWrittenBytes() {
        return offs - mark;
    }

    public OutputStream getDelegate() {
        return delegate;
    }

    public int getCurrSplitFileCounter() {
        return delegate instanceof SplitOutputStream ? ((SplitOutputStream)delegate).getCurrSplitFileCounter() : 0;
    }

    public long getOffsLocalHeaderRelative() throws IOException {
        if (offs == 4)
            return 4;
        if (delegate instanceof SplitOutputStream)
            return ((SplitOutputStream)delegate).getFilePointer();
        return offs;
    }

    public long getSplitLength() {
        return delegate instanceof SplitOutputStream ? ((SplitOutputStream)delegate).getSplitLength() : 0;
    }

    public void seek(long pos) throws IOException {
        if (delegate instanceof SplitOutputStream)
            ((SplitOutputStream)delegate).seek(pos);
    }

    public void writeInt(int val) throws IOException {
        Raw.writeIntLittleEndian(intByte, 0, val);
        delegate.write(intByte);
        offs += 4;
    }

    public void writeBytes(byte... buf) throws IOException {
        delegate.write(buf);
        offs += buf.length;
    }

    public void writeBytes(byte[] buf, int offs, int len) throws IOException {
        delegate.write(buf, offs, len);
        this.offs += len;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
