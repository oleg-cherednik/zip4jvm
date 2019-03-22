package net.lingala.zip4j.io;

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
    // TODO temporary
    public long offs;

    public OutputStream getDelegate() {
        return delegate;
    }

    public void addTotalBytesWritten(int delta) {
        offs += delta;
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

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
