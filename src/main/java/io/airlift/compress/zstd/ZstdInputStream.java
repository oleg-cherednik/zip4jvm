package io.airlift.compress.zstd;

import ru.olegcherednik.zip4jvm.io.in.DataInput;

import io.airlift.compress.zstd.compress.ZstdFrameDecompressorNew;
import io.airlift.compress.zstd.dep.OutByteArrayWithOffs;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 25.10.2026
 */
public class ZstdInputStream extends InputStream {

    private final ZstdFrameDecompressorNew decompressor;
    private final byte[] buf = new byte[100_000_000];

    private int offs;
    private int length;
    private boolean eof;

    public ZstdInputStream(DataInput in) {
        decompressor = new ZstdFrameDecompressorNew(in);
    }

    private void readNextFrame() {
        int frameLength = decompressor.readNextFrame(new OutByteArrayWithOffs(buf));

        if (frameLength < 0)
            eof = true;
        else {
            offs = 0;
            length = frameLength;
        }
    }

    // ---------- InputStream----------

    @Override
    public int read() throws IOException {
        if (eof)
            return IOUtils.EOF;

        if (offs == length)
            readNextFrame();

        return eof ? IOUtils.EOF : buf[offs++] & 0xFF;
    }

}
