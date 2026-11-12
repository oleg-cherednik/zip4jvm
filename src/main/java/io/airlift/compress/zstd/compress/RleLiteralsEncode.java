package io.airlift.compress.zstd.compress;

import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.LiteralsSectionHeader;
import io.airlift.compress.zstd.OutByteArrayWithOffs;

import static io.airlift.compress.zstd.Constants.RLE_LITERALS_BLOCK;

/**
 * @author Oleg Cherednik
 * @since 12.11.2026
 */
public class RleLiteralsEncode {

    public void encode(OutByteArrayWithOffs out, ByteArrayWithOffs in) {
        LiteralsSectionHeader header = new LiteralsSectionHeader();
        header.setRegeneratedSize(in.getOffs());

        int headerSize;

        if (header.getRegeneratedSize() < 31) {
            headerSize = 1;
            header.setBlockType(LiteralsSectionHeader.BlockType.RLE_LITERALS_BLOCK);
        } else if (header.getRegeneratedSize() < 4095) {
            headerSize = 2;
            header.setBlockType(LiteralsSectionHeader.BlockType.COMPRESSED_LITERALS_BLOCK);
        } else {
            headerSize = 3;
            header.setBlockType(LiteralsSectionHeader.BlockType.TREELESS_LITERALS_BLOCK);
        }

        if (headerSize + 1 > out.available())
            return;

        if (headerSize == 1)    // 2 - 1 - 5
            out.putByte((byte) (RLE_LITERALS_BLOCK | (header.getRegeneratedSize() << 3)));
        else if (headerSize == 2)   // 2 - 2 - 12
            out.putShort((short) (RLE_LITERALS_BLOCK | (1 << 2) | (header.getRegeneratedSize() << 4)));
        else    // 2 - 2 - 20
            out.putInt(RLE_LITERALS_BLOCK | 3 << 2 | header.getRegeneratedSize() << 4);

        out.putByte(in.buf[0]);
    }

}
