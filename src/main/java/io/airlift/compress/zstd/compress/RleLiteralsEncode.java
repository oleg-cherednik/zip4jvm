package io.airlift.compress.zstd.compress;

import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.LiteralsSectionHeader;
import io.airlift.compress.zstd.OutByteArrayWithOffs;

/**
 * @author Oleg Cherednik
 * @since 12.11.2026
 */
public class RleLiteralsEncode {

    public void encode(OutByteArrayWithOffs out, ByteArrayWithOffs in) {
        LiteralsSectionHeader.RawRle header = LiteralsSectionHeader.createForRle();
        header.withRegeneratedSize(in.getOffs());

        if (header.getHeaderSize() + 1 <= out.available()) {
            header.encode(out);
            out.putByte(in.buf[0]);
        }
    }

}
