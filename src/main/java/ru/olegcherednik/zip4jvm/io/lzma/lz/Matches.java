package ru.olegcherednik.zip4jvm.io.lzma.lz;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Oleg Cherednik
 * @since 14.02.2020
 */
public final class Matches {

    public final int[] len;
    public final int[] dist;
    @Getter
    @Setter
    private int count;

    Matches(int maxCount) {
        len = new int[maxCount];
        dist = new int[maxCount];
    }

    public void incCount() {
        count++;
    }

    public void decCount() {
        count--;
    }
}
