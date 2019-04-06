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
    private final byte[] shortByte = new byte[2];
    private final byte[] longByte = new byte[8];

    private final OutputStream delegate;
    private long offs;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private long mark;

    public void mark() {
        mark = offs;
    }

    public long getWrittenBytesAmount() {
        return offs - mark;
    }

    public OutputStream getDelegate() {
        return delegate;
    }

    public int getCurrSplitFileCounter() {
        return delegate instanceof SplitOutputStream ? ((SplitOutputStream)delegate).getCurrSplitFileCounter() : 0;
    }

    public long getFilePointer() throws IOException {
        return delegate instanceof SplitOutputStream ? ((SplitOutputStream)delegate).getFilePointer() : offs;
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

    // 2 bytes (16 bit)
    public void writeWord(short val) throws IOException {
        writeShort(val);
    }

    // 4 bytes (32 bit)
    public void writeDword(int val) throws IOException {
        writeInt(val);
    }

    public void writeDword(long val) throws IOException {
        writeLongAsInt(val);
    }

    public void writeInt(int val) throws IOException {
        Raw.writeIntLittleEndian(intByte, 0, val);
        delegate.write(intByte);
        offs += intByte.length;
    }

    public void writeShort(short val) throws IOException {
        Raw.writeShortLittleEndian(shortByte, 0, val);
        delegate.write(shortByte);
        offs += shortByte.length;
    }

    public void writeBytes(byte... buf) throws IOException {
        delegate.write(buf);
        offs += buf.length;
    }

    public void writeBytes(byte[] buf, int offs, int len) throws IOException {
        delegate.write(buf, offs, len);
        this.offs += len;
    }

    public void writeLong(long val) throws IOException {
        Raw.writeLongLittleEndian(longByte, 0, val);
        delegate.write(longByte);
        offs += longByte.length;
    }

    public void writeLongAsInt(long val) throws IOException {
        Raw.writeLongLittleEndian(longByte, 0, val);
        System.arraycopy(longByte, 0, intByte, 0, 4);
        delegate.write(intByte);
        offs += intByte.length;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
