package ru.olegcherednik.zip4jvm.io.zstd;

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * @author Oleg Cherednik
 * @since 07.11.2021
 */
public class ZstdOutputStream extends OutputStream {

    private final DataOutput out;
    private final ZstdCompressor compressor;

    public ZstdOutputStream(DataOutput out, CompressionLevel compressionLevel) {
        this.out = out;
        compressor = new ZstdCompressor(compressionLevel(compressionLevel));
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

    private ByteBuffer inputBuffer;

    public void write(final byte[] buf, int offs, final int len) throws IOException {
        if (inputBuffer == null)
            inputBuffer = ByteBuffer.allocate(500_000);

        inputBuffer.put(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        if (inputBuffer == null)
            return;

        ByteBuffer outputBuffer = ByteBuffer.allocate(500_000);
        compressor.compress(inputBuffer, outputBuffer);

        int len = outputBuffer.position();
        byte[] buf = new byte[len];
        outputBuffer.get(buf, 0, buf.length);

        out.write(buf, 0, buf.length);
        inputBuffer = null;
    }

}
