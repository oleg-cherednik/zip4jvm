package io.airlift.compress;

import lombok.Getter;

/**
 * @author Oleg Cherednik
 * @since 16.07.2026
 */
public class Foo {

    @Getter
    private byte[] compressed;
    @Getter
    private int offs;

    public Foo(int maxLength) {
        compressed = new byte[maxLength];
    }

    public int getMaxLength() {
        return compressed.length;
    }
}
