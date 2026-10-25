package io.airlift.compress.zstd;

import ru.olegcherednik.zip4jvm.io.in.DataInput;

import io.airlift.compress.zstd.dep.OutByteArrayWithOffs;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 25.10.2026
 */
@RequiredArgsConstructor
public class ZstdInputStream extends InputStream {

    private final DataInput in;
    private final ZstdDecompressor decompressor = new ZstdDecompressor();
    private final byte[] buf = new byte[100_000_000];

    private int offs;
    private int length;
    private boolean eof;

    private void foo() {
        length = decompressor.decompress(in, new OutByteArrayWithOffs(buf));
    }

    // ---------- InputStream----------

    @Override
    public int read() throws IOException {
        if (!eof && length == 0) {
            foo();
            offs = 0;
        }

        if (offs == length)
            eof = true;

        return eof ? IOUtils.EOF : buf[offs++] & 0xFF;
    }

}
