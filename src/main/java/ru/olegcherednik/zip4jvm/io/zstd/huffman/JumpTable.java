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
    private final int compressedSizeStream1;
    // 2 bytes
    private final int compressedSizeStream2;
    // 2 bytes
    private final int compressedSizeStream3;

    public static JumpTable read(Buffer inputBase) {
        int compressedSizeStream1 = inputBase.getShort();
        int compressedSizeStream2 = inputBase.getShort();
        int compressedSizeStream3 = inputBase.getShort();
        return new JumpTable(compressedSizeStream1, compressedSizeStream2, compressedSizeStream3);
    }

}
