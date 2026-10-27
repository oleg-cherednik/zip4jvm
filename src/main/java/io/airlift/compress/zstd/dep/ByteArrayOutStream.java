package io.airlift.compress.zstd.dep;

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
public final class ByteArrayOutStream implements OutStream {

    private final byte[] buf;
    @Getter
    private int offs;
    @Setter
    @Getter
    private int limit;

    @Override
    public int available() {
        return buf.length - offs;
    }

    @Override
    public void copyMemory(byte[] buf, int offs, int bytes) {
        System.arraycopy(buf, offs, this.buf, this.offs, bytes);
        this.offs += bytes;
    }

    @Override
    public String toString() {
        return String.format("size: %s, offs: %s", buf.length, offs);
    }

}
