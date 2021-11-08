package ru.olegcherednik.zip4jvm.io.zstd;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.zstd.frame.ZstdFrameDecompressor;

import java.io.IOException;
import java.io.InputStream;

/**
 * https://github.com/facebook/zstd/blob/dev/doc/zstd_compression_format.md#frame_header
 *
 * @author Oleg Cherednik
 * @since 06.11.2021
 */
public class ZstdInputStream extends InputStream {

    private static final int SIGNATURE = 0xFD2FB528;
    private static final int SIGNATURE_V07 = 0xFD2FB527;

    private final DataInput in;
    private final ZstdFrameDecompressor decompressor = new ZstdFrameDecompressor();

    public ZstdInputStream(DataInput in) throws IOException {
//        verifyMagic(in);
        this.in = in;
    }

    private static void verifyMagic(DataInput in) throws IOException {
        int signature = in.readDwordSignature();

        if (signature == SIGNATURE)
            return;
        if (signature == SIGNATURE_V07)
            throw new Zip4jvmException("Data encoded in unsupported ZSTD v0.7 format");
        throw new Zip4jvmException("Invalid ZSTD signature: " + signature);
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
            outputBase = new byte[500_000];
            Buffer inputBase = new Buffer(in.readBytes(500_000));
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


