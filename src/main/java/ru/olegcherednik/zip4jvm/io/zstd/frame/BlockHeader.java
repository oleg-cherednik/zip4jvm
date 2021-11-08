package ru.olegcherednik.zip4jvm.io.zstd.frame;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.zstd.Buffer;

/**
 * @author Oleg Cherednik
 * @since 08.11.2021
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockHeader {

    private final boolean last;
    private final Type type;
    private final int size;

    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public enum Type {
        RAW(0),
        RLE(1),
        COMPRESSED(2);

        private final int id;

        public static Type parseId(int id) {
            for (Type type : values())
                if (type.id == id)
                    return type;

            throw new Zip4jvmException("Unknown block type: " + id);
        }
    }

    public static BlockHeader read(Buffer inputBase) {
        int data = readData(inputBase);
        boolean last = (data & 1) != 0;
        Type type = Type.parseId((data >>> 1) & 0b11);
        int size = (data >>> 3) & 0x1F_FFFF; // 21 bits

        return new BlockHeader(last, type, size);
    }

    private static int readData(Buffer inputBase) {
        int one = inputBase.getByte();
        int two = inputBase.getByte();
        int three = inputBase.getByte();
        return three << 16 | two << 8 | one;
    }

}
