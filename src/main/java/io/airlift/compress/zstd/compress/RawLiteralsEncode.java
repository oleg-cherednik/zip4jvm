package io.airlift.compress.zstd.compress;

import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.LiteralsSectionHeader;
import io.airlift.compress.zstd.io.OutByteArrayWithOffs;

/**
 * @author Oleg Cherednik
 * @since 13.11.2026
 */
public class RawLiteralsEncode {

    public void encode(OutByteArrayWithOffs out, ByteArrayWithOffs in) {
        LiteralsSectionHeader.RawRle header = LiteralsSectionHeader.createForRaw();
        header.withRegeneratedSize(in.getOffs());

        if (header.getHeaderSize() + header.getRegeneratedSize() <= out.available()) {
            header.encode(out);
            out.copyMemory(in);
        }
    }

}
