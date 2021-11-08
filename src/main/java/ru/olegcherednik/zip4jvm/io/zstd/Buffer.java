package ru.olegcherednik.zip4jvm.io.zstd;

import lombok.Getter;

/**
 * @author Oleg Cherednik
 * @since 08.11.2021
 */
@Getter
public final class Buffer {

    private final byte[] buf;
    private int offs;

    public Buffer(int size) {
        buf = new byte[size];
    }

    public void incOffs(int size) {
        offs += size;
    }

}
