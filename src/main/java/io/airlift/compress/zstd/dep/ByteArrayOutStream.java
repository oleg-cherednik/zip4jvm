package io.airlift.compress.zstd.dep;

import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.compress.XXHash64;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static io.airlift.compress.zstd.Constants.SIZE_OF_BYTE;

/**
 * This class is used only for output.
 *
 * @author Oleg Cherednik
 * @since 03.09.2026
 */
@RequiredArgsConstructor
public final class ByteArrayOutStream {

    private final byte[] buf;
    @Getter
    private int offs;
    @Setter
    @Getter
    private int limit;
    private XXHash64 checksum;

    public void initChecksum() {
        checksum = new XXHash64();
    }

    public long getChecksum() {
        return checksum.getValue();
    }

    public void cleanChecksum() {
        checksum = null;
    }

    public int available() {
        return buf.length - offs;
    }

    public void putByte(byte x) {
        buf[offs] = x;

        if (checksum != null)
            checksum.update(buf, offs, SIZE_OF_BYTE);

        offs += SIZE_OF_BYTE;
    }

    public void copyMemory(ByteArrayWithOffs in, int bytes) {
        int nowRead = in.read(buf, offs, bytes);

        if (checksum != null)
            checksum.update(buf, offs, nowRead);

        offs += nowRead;
    }

    @Override
    public String toString() {
        return String.format("size: %s, offs: %s", buf.length, offs);
    }

}
