package io.airlift.compress.zstd.compress;

import io.airlift.compress.zstd.io.ByteArrayWithOffs;
import io.airlift.compress.zstd.model.LiteralsSectionHeader;
import io.airlift.compress.zstd.Out;

/**
 * @author Oleg Cherednik
 * @since 12.11.2026
 */
public class RleLiteralsEncode {

    public void encode(Out out, ByteArrayWithOffs in) {
        LiteralsSectionHeader.RawRle header = LiteralsSectionHeader.createForRle();
        header.withRegeneratedSize(in.getOffs());

        if (header.getHeaderSize() + 1 <= out.available()) {
            header.encode(out);
            out.writeByte(in.buf[0]);
        }
    }

}
