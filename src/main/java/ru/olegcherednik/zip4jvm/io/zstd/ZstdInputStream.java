package ru.olegcherednik.zip4jvm.io.zstd;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * https://github.com/facebook/zstd/blob/dev/doc/zstd_compression_format.md#frame_header
 *
 * @author Oleg Cherednik
 * @since 06.11.2021
 */
public class ZstdInputStream extends InputStream {

    private final DataInput in;
    private final ZstdDecompressor decompressor;

    public ZstdInputStream(DataInput in) throws IOException {
//        verifyMagic(in);
        this.in = in;
        decompressor = new ZstdDecompressor();
    }

    @Override
    public int read() throws IOException {
        return 0;
    }

    private ByteBuffer output;
    private int offs;

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        if (output == null) {
            in.mark("start");
            ByteBuffer input = ByteBuffer.wrap(in.readBytes(500_000));
            output = ByteBuffer.allocate(500_000);
            decompressor.decompress(input, output);
            this.offs = 0;
            in.seek("start");
            in.skip(output.position());
        }

        int minLen = Math.min(len, output.position() - this.offs);

        if (minLen == 0) {
            output = null;
            return IOUtils.EOF;
        }

        System.arraycopy(output.array(), this.offs, buf, offs, minLen);

        if (minLen == output.position())
            output = null;
        else
            this.offs += minLen;

        return minLen;
    }

}


