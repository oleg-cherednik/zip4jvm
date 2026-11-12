package io.airlift.compress.zstd.compress;

import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.LiteralsSectionHeader;
import io.airlift.compress.zstd.OutByteArrayWithOffs;

/**
 * @author Oleg Cherednik
 * @since 13.11.2026
 */
public class RawLiteralsEncode {

    public void encode(OutByteArrayWithOffs out, ByteArrayWithOffs in) {
        LiteralsSectionHeader header = new LiteralsSectionHeader();
        header.setBlockType(LiteralsSectionHeader.BlockType.RAW_LITERALS_BLOCK);
        header.setRegeneratedSize(in.getOffs());

        if (header.getHeaderSize() + header.getRegeneratedSize() <= out.available()) {
            header.encode(out);
            out.copyMemory(in);
        }
    }

}
