package ru.olegcherednik.zip4jvm.io.zstd;

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.io.zstd.frame.ZstdFrameCompressor;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 07.11.2021
 */
public class ZstdOutputStream extends OutputStream {

    private final DataOutput out;
    private final CompressionLevel compressionLevel;
    private final ZstdFrameCompressor compressor;

    public ZstdOutputStream(DataOutput out, CompressionLevel compressionLevel) {
        this.out = out;
        this.compressionLevel = compressionLevel;
        compressor = new ZstdFrameCompressor();
    }

    private static int compressionLevel(CompressionLevel compressionLevel) {
//        public static final int DEFAULT_COMPRESSION_LEVEL = 3;
//        private static final int MAX_COMPRESSION_LEVEL = 22;
//        if (compressionLevel == CompressionLevel.SUPER_FAST)
//            return 3;
//        if (compressionLevel == CompressionLevel.FAST)
//            return 5;
//        if (compressionLevel == CompressionLevel.NORMAL)
//            return 11;
//        if (compressionLevel == CompressionLevel.MAXIMUM)
//            return 17;
//        return 1;
        return 3;
    }

    @Override
    public void write(int b) throws IOException {
        int a = 0;
        a++;
    }

    private byte[] inputBuffer;
    private int offs;

    @Override
    public void write(final byte[] buf, int offs, final int len) throws IOException {
        if (inputBuffer == null) {
            inputBuffer = new byte[500_000];
            offs = 0;
        }

        System.arraycopy(buf, offs, inputBuffer, this.offs, len);
        this.offs += len;
    }

    @Override
    public void close() throws IOException {
        if (inputBuffer == null)
            return;

        byte[] inputBase = new byte[offs];
        System.arraycopy(inputBuffer, 0, inputBase, 0, inputBase.length);

        Buffer outputBase = new Buffer(500_000);
        int written = compressor.compress(inputBase, outputBase, compressionLevel(compressionLevel));

        out.write(outputBase.getBuf(), 0, written);
        inputBuffer = null;
    }

}
