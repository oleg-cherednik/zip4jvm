package ru.olegcherednik.zip4jvm.io.lzma.xz.lz;

import ru.olegcherednik.zip4jvm.io.lzma.xz.LzmaInputStream;

/**
 * @author Oleg Cherednik
 * @since 13.02.2020
 */
public enum MatchFinder {
    HASH_CHAIN {
        @Override
        public LZEncoder createEncoder(LzmaInputStream.Properties properties, int extraSizeAfter) {
            return new HashChain(properties, extraSizeAfter);
        }
    },
    BINARY_TREE {
        @Override
        public LZEncoder createEncoder(LzmaInputStream.Properties properties, int extraSizeAfter) {
            return new BinaryTree(properties, extraSizeAfter);
        }
    };

    public abstract LZEncoder createEncoder(LzmaInputStream.Properties properties, int extraSizeAfter);
}
