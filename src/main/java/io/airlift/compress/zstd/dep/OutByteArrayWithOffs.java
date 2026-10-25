package io.airlift.compress.zstd.dep;

import ru.olegcherednik.zip4jvm.io.in.DataInput;

import io.airlift.compress.zstd.ByteArrayWithOffs;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static io.airlift.compress.zstd.Constants.SIZE_OF_BYTE;
import static io.airlift.compress.zstd.Constants.SIZE_OF_INT;
import static io.airlift.compress.zstd.Constants.SIZE_OF_LONG;

/**
 * This class is used only for output.
 *
 * @author Oleg Cherednik
 * @since 03.09.2026
 */
@RequiredArgsConstructor
public final class OutByteArrayWithOffs {

    public final byte[] buf;
    @Getter
    private int offs;
    @Setter
    @Getter
    private int limit;

    public void setOffs(int offs) {
        this.offs = offs;
    }

    public int available() {
        return buf.length - offs;
    }

    public void putByte(byte x) {
        buf[offs] = x;
        offs += SIZE_OF_BYTE;
    }

    public void putInt(int x) {
        buf[offs] = (byte) (x & 0xFF);
        buf[offs + 1] = (byte) ((x & 0xFF00) >> 8);
        buf[offs + 2] = (byte) ((x & 0xFF0000) >> 8 * 2);
        buf[offs + 3] = (byte) ((x & 0xFF000000) >> 8 * 3);
        offs += SIZE_OF_INT;
    }

    public void putLong(long x) {
        buf[offs] = (byte) (x & 0xFF);
        buf[offs + 1] = (byte) ((x & 0xFF00) >> 8);
        buf[offs + 2] = (byte) ((x & 0xFF0000) >> 8 * 2);
        buf[offs + 3] = (byte) ((x & 0xFF000000) >> 8 * 3);
        buf[offs + 4] = (byte) ((x & 0xFF00000000L) >> 8 * 4);
        buf[offs + 5] = (byte) ((x & 0xFF0000000000L) >> 8 * 5);
        buf[offs + 6] = (byte) ((x & 0xFF000000000000L) >> 8 * 6);
        buf[offs + 7] = (byte) ((x & 0xFF00000000000000L) >> 8 * 7);
        offs += SIZE_OF_LONG;
    }

    public void copyMemory(ByteArrayWithOffs in, int bytes) {
        offs += in.read(buf, offs, bytes);
    }

    public void copyMemory(DataInput in, int bytes) {
        offs += in.read(buf, offs, bytes);
    }

    @Override
    public String toString() {
        return String.format("size: %s, offs: %s", buf.length, offs);
    }

}
