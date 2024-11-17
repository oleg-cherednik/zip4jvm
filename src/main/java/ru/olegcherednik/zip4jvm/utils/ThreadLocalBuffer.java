package ru.olegcherednik.zip4jvm.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 18.11.2024
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ThreadLocalBuffer {

    private static final ThreadLocal<byte[]> ONE_BYTE = ThreadLocal.withInitial(() -> new byte[1]);

    public static byte[] getOne() {
        return ONE_BYTE.get();
    }

}
