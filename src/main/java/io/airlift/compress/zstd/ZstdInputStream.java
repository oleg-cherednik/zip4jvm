package io.airlift.compress.zstd;

import ru.olegcherednik.zip4jvm.io.in.DataInput;

import io.airlift.compress.zstd.compress.SlideWindow;
import io.airlift.compress.zstd.compress.ZstdFrameDecompressor;
import io.airlift.compress.zstd.dep.ByteArrayOutStream;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 25.10.2026
 */
public class ZstdInputStream extends InputStream {

    private final SlideWindow slideWindow = new SlideWindow();
    private final ZstdFrameDecompressor decompressor;
    private final byte[] buf = new byte[100_000_000];

    private int offs;
    private int length;
    private boolean eof;

    public ZstdInputStream(DataInput in) {
        decompressor = new ZstdFrameDecompressor(in);
    }

    private void readNext() {
        int nowRead = decompressor.readNextDataBlock(slideWindow, new ByteArrayOutStream(buf));

        if (nowRead < 0)
            eof = true;
        else {
            offs = 0;
            length = nowRead;
        }
    }

    // ---------- InputStream----------

    @Override
    public int read() throws IOException {
        if (eof)
            return IOUtils.EOF;

        if (offs == length)
            readNext();

        return eof ? IOUtils.EOF : buf[offs++] & 0xFF;
    }

}
