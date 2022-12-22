package ru.olegcherednik.zip4jvm.io.in.buf;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.data.BaseDataInputNew;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.12.2022
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ByteArrayDataInputNew extends BaseDataInputNew {

    private final byte[] buf;
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

    // ---------- ReadBuffer ----------

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        len = Math.min(len, this.buf.length - this.offs);
        System.arraycopy(this.buf, this.offs, buf, offs, len);
        this.offs += len;
        return len;
    }
}
