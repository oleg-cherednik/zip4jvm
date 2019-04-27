package net.lingala.zip4j.io;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.lingala.zip4j.utils.Raw;
import org.apache.commons.lang.ArrayUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

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

    private final SplitOutputStream delegate;
    private long offs;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final Map<String, Long> mark = new HashMap<>();

    public void mark(String id) {
        mark.put(id, offs);
    }

    public long getWrittenBytesAmount(String id) {
        return offs - mark.getOrDefault(id, 0L);
    }

    public int getCurrSplitFileCounter() {
        return delegate.getCurrSplitFileCounter();
    }

    public long getFilePointer() throws IOException {
        return delegate.getFilePointer();
    }

    public long getOffsLocalHeaderRelative() throws IOException {
        if (offs == 4)
            return 4;
        return delegate.getFilePointer();
    }

    public void seek(long pos) throws IOException {
        delegate.seek(pos);
    }

    // 2 bytes (16 bit)
    public void writeWord(short val) throws IOException {
        Raw.writeShortLittleEndian(shortByte, 0, val);
        delegate.write(shortByte);
        offs += shortByte.length;
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

    public void writeBytes(byte... buf) throws IOException {
        if (ArrayUtils.isNotEmpty(buf)) {
            delegate.write(buf);
            offs += buf.length;
        }
    }

    public void writeBytes(byte[] buf, int offs, int len) throws IOException {
        delegate.write(buf, offs, len);
        this.offs += len;
    }

    public void writeString(String str, @NonNull Charset charset) throws IOException {
        if (str != null)
            writeBytes(str.getBytes(charset));
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
