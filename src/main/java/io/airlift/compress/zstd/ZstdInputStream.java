package io.airlift.compress.zstd;

import io.airlift.compress.zstd.compress.SlideWindow;
import io.airlift.compress.zstd.compress.ZstdFrameDecompressor;
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

    private int offs;
    private int length;
    private boolean eof;

    public ZstdInputStream(In in) {
        decompressor = new ZstdFrameDecompressor(in);
    }

    private void readNext() throws IOException {
        int nowRead = decompressor.readNextDataBlock(slideWindow);

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

        return eof ? IOUtils.EOF : slideWindow.getBuf()[slideWindow.getBlockStart() + offs++] & 0xFF;
    }

}
