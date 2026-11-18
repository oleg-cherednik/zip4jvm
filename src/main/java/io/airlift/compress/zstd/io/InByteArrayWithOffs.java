package io.airlift.compress.zstd.io;

import io.airlift.compress.zstd.In;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;

/**
 * This class is used only for input.
 *
 * @author Oleg Cherednik
 * @since 03.09.2026
 */
@Getter
@Setter
public final class InByteArrayWithOffs implements In {

    private final byte[] buf;
    private int offs;
    private int size;
    private int limit;

    public InByteArrayWithOffs(byte[] buf) {
        this.buf = buf;
        limit = buf.length;
    }

    // ---------- CompressIn ----------

    @Override
    public int available() {
        return limit - offs;
    }

    @Override
    public int read(byte[] buf, int offs, int len) {
        int available = available();

        if (available <= 0)
            return IOUtils.EOF;

        int nowRead = Math.min(available, len);
        System.arraycopy(this.buf, this.offs, buf, offs, nowRead);
        this.offs += nowRead;
        return nowRead;
    }

    @Override
    public String toString() {
        return String.format("size: %s, offs: %s", buf.length, offs);
    }

}
