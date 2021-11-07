package ru.olegcherednik.zip4jvm.io.zstd;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;
import java.io.InputStream;

/**
 * https://github.com/facebook/zstd/blob/dev/doc/zstd_compression_format.md#frame_header
 *
 * @author Oleg Cherednik
 * @since 06.11.2021
 */
public class ZstdInputStream extends InputStream {

    private final DataInput in;
    private final ZstdFrameDecompressor decompressor;

    public ZstdInputStream(DataInput in) throws IOException {
//        verifyMagic(in);
        this.in = in;
        decompressor = new ZstdFrameDecompressor();
    }

    @Override
    public int read() throws IOException {
        return 0;
    }

    private byte[] outputBase;
    private int offs;
    private int written;

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        if (outputBase == null) {
            in.mark("start");
            byte[] inputBase = in.readBytes(500_000);
            outputBase = new byte[500_000];
            written = decompressor.decompress(inputBase, outputBase);
            this.offs = 0;
            in.seek("start");
            in.skip(written);
        }

        int minLen = Math.min(len, written - this.offs);

        if (minLen == 0) {
            outputBase = null;
            return IOUtils.EOF;
        }

        System.arraycopy(outputBase, this.offs, buf, offs, minLen);
        this.offs += minLen;
        return minLen;
    }

}


