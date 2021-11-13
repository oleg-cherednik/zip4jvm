package ru.olegcherednik.zip4jvm.io.zstd.huffman;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.zstd.Buffer;

/**
 * see 3.1.1.3.1.6
 *
 * @author Oleg Cherednik
 * @since 13.11.2021
 */
@Getter
@RequiredArgsConstructor
public class JumpTable {

    // 2 bytes
    private final int streamOneSize;
    // 2 bytes
    private final int streamTwoSize;
    // 2 bytes
    private final int streamThreeSize;

    public static JumpTable read(Buffer inputBase) {
        int streamOneSize = inputBase.getShort();
        int streamTwoSize = inputBase.getShort();
        int streamThreeSize = inputBase.getShort();
        return new JumpTable(streamOneSize, streamTwoSize, streamThreeSize);
    }

}
