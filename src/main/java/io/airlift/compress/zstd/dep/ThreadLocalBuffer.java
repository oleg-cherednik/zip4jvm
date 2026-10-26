package io.airlift.compress.zstd.dep;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;

/**
 * @author Oleg Cherednik
 * @since 26.10.2026
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ThreadLocalBuffer {

    private static final ThreadLocal<byte[]> BUFFER = new ThreadLocal<>();

    /**
     * Returns the thread local buffer with at least {@code size} bytes; the buffer is replaced with a bigger one when
     * required. The returned buffer could be longer than {@code size}.
     */
    public static byte[] get(int size) {
        byte[] buf = BUFFER.get();

        if (buf == null || buf.length < size) {
            buf = new byte[size];
            BUFFER.set(buf);
        }

        return buf;
    }

}
