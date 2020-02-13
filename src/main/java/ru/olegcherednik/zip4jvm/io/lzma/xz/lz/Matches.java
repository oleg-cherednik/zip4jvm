package ru.olegcherednik.zip4jvm.io.lzma.xz.lz;

public final class Matches {

    public final int[] len;
    public final int[] dist;
    public int count;

    Matches(int maxCount) {
        len = new int[maxCount];
        dist = new int[maxCount];
    }
}
