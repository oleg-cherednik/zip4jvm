package io.airlift.compress.zstd.compress;

import io.airlift.compress.zstd.Out;
import io.airlift.compress.zstd.io.ByteArrayWithOffs;
import io.airlift.compress.zstd.io.RandomAccess;
import io.airlift.compress.zstd.model.LiteralsSectionHeader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 12.11.2026
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RleLiteralsEncode {

    public static void encode(Out out, ByteArrayWithOffs in) {
        LiteralsSectionHeader.RawRle header = LiteralsSectionHeader.createForRle();
        header.withRegeneratedSize(in.getOffs());

        if (header.getHeaderSize() + 1 <= out.available()) {
            header.encode(out);
            out.writeByte(in.buf[0]);
        }
    }

    public static void encode(Out out, RandomAccess in) {
        LiteralsSectionHeader.RawRle header = LiteralsSectionHeader.createForRle();
        header.withRegeneratedSize(in.getOffs());

        if (header.getHeaderSize() + 1 <= out.available()) {
            header.encode(out);

            int offs = in.getOffs();
            in.setOffs(0);
            out.writeByte(in.getByte());
            in.setOffs(offs);
        }
    }

}
