package io.airlift.compress.zstd.io;

import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.dep.InByteArrayWithOffs;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static io.airlift.compress.zstd.Constants.SIZE_OF_BYTE;
import static io.airlift.compress.zstd.Constants.SIZE_OF_INT;
import static io.airlift.compress.zstd.Constants.SIZE_OF_SHORT;

/**
 * @author Oleg Cherednik
 * @since 03.09.2026
 */
@Getter
@Setter
@RequiredArgsConstructor
public class OutByteArrayWithOffs implements RandomAccessOut {

    public final byte[] buf;
    private int offs;
    private int limit;

    // ---------- Out ----------

    @Override
    public int available() {
        return buf.length - offs;
    }

    @Override
    public void putByte(byte x) {
        buf[offs] = x;
        offs += SIZE_OF_BYTE;
    }

    @Override
    public void putShort(short x) {
        buf[offs] = (byte) (x & 0xFF);
        buf[offs + 1] = (byte) ((x & 0xFF00) >> 8);
        offs += SIZE_OF_SHORT;
    }

    @Override
    public void putInt(int x) {
        buf[offs] = (byte) (x & 0xFF);
        buf[offs + 1] = (byte) ((x & 0xFF00) >> 8);
        buf[offs + 2] = (byte) ((x & 0xFF0000) >> 8 * 2);
        buf[offs + 3] = (byte) ((x & 0xFF000000) >> 8 * 3);
        offs += SIZE_OF_INT;
    }

    @Override
    public void copyMemory(ByteArrayWithOffs in) {
        System.arraycopy(in.buf, 0, buf, offs, in.getOffs());
        offs += in.getOffs();
    }

    @Override
    public void copyMemory(RandomAccessOut rao) {
        int nowRead = rao.read(buf, offs, rao.available());
        offs += nowRead;
    }

    @Override
    public void copyMemory(InByteArrayWithOffs in, int size) {
        System.arraycopy(in.buf, in.getOffs(), buf, offs, size);
        offs += size;
    }

    // ---------- Objects ----------

    @Override
    public String toString() {
        return String.format("limit: %s, offs: %s", limit, offs);
    }

}
