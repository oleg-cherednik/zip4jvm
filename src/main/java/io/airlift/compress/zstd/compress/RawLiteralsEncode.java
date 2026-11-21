package io.airlift.compress.zstd.compress;

import io.airlift.compress.zstd.Out;
import io.airlift.compress.zstd.io.ByteArrayWithOffs;
import io.airlift.compress.zstd.io.RandomAccessOut;
import io.airlift.compress.zstd.model.LiteralsSectionHeader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 13.11.2026
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RawLiteralsEncode {

    public static void encode(Out out, ByteArrayWithOffs in) {
        LiteralsSectionHeader.RawRle header = LiteralsSectionHeader.createForRaw();
        header.withRegeneratedSize(in.getOffs());

        if (header.getHeaderSize() + header.getRegeneratedSize() <= out.available()) {
            header.encode(out);
            out.copyMemory(in);
        }
    }

    public static void encode(Out out, RandomAccessOut in) {
        LiteralsSectionHeader.RawRle header = LiteralsSectionHeader.createForRaw();
        header.withRegeneratedSize(in.getOffs());

        if (header.getHeaderSize() + header.getRegeneratedSize() <= out.available()) {
            header.encode(out);
            out.copyMemory(in.slice(0, in.getOffs()));
        }
    }

}
