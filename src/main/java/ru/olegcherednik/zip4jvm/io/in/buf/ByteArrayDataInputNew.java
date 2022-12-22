package ru.olegcherednik.zip4jvm.io.in.buf;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.Endianness;
import ru.olegcherednik.zip4jvm.io.in.data.BaseDataInputNew;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputNew;

import java.io.IOException;

/**
 * {@link DataInputNew} base on the given byte array
 *
 * @author Oleg Cherednik
 * @since 22.12.2022
 */
@RequiredArgsConstructor
public class ByteArrayDataInputNew extends BaseDataInputNew {

    private final byte[] buf;
    @Getter
    private final Endianness endianness;
    private int offs;

    // ---------- RandomAccess ----------

    @Override
    public long skip(long bytes) throws IOException {
        bytes = Math.min(bytes, buf.length - offs);
        offs += bytes;
        return bytes;
    }

    @Override
    public void seek(long absoluteOffs) throws IOException {
        if (absoluteOffs >= 0 && absoluteOffs < buf.length)
            offs = (int)absoluteOffs;
    }

    // ---------- DataInputNew ----------

    @Override
    public long getAbsoluteOffs() {
        return offs;
    }

    @Override
    public long size() throws IOException {
        return buf.length;
    }

    // ---------- ReadBuffer ----------

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        len = Math.min(len, this.buf.length - this.offs);
        System.arraycopy(this.buf, this.offs, buf, offs, len);
        this.offs += len;
        return len;
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return "offs: " + offs + " (0x" + Long.toHexString(offs) + ')';
    }
}
