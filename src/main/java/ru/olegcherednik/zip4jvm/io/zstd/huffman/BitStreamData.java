package ru.olegcherednik.zip4jvm.io.zstd.huffman;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author Oleg Cherednik
 * @since 14.11.2021
 */
@Getter
@Setter
@RequiredArgsConstructor
public class BitStreamData {

    private final int offs;
    private final int size;
    private long bits;
    private int currentAddress;
    private int bitsConsumed;
    private int outputAddress;

    public int getEndOffs() {
        return offs + size;
    }

}
