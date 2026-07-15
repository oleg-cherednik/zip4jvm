package io.airlift.compress;

import lombok.Getter;

import static sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET;

/**
 * @author Oleg Cherednik
 * @since 16.07.2026
 */
public class Foo {

    @Getter
    private byte[] compressed;
    @Getter
    private int offs;
    @Getter
    private long outputAddress = ARRAY_BYTE_BASE_OFFSET;

    public Foo(int maxLength) {
        compressed = new byte[maxLength];
    }

    public int getMaxLength() {
        return compressed.length;
    }
}
