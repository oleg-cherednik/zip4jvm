package ru.olegcherednik.zip4jvm.io.zstd;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 07.11.2021
 */
public class ZstdOutputStream extends OutputStream {

    private final com.github.luben.zstd.ZstdOutputStream out;

    public ZstdOutputStream(DataOutput out, CompressionLevel compressionLevel) throws IOException {
        this.out = new com.github.luben.zstd.ZstdOutputStream(new Decorator(out), compressionLevel(compressionLevel));
    }

    private static int compressionLevel(CompressionLevel compressionLevel) {
        if (compressionLevel == CompressionLevel.SUPER_FAST)
            return 3;
        if (compressionLevel == CompressionLevel.FAST)
            return 5;
        if (compressionLevel == CompressionLevel.NORMAL)
            return 11;
        if (compressionLevel == CompressionLevel.MAXIMUM)
            return 17;
        return 11;
    }

    @Override
    public void write(int val) throws IOException {
        out.write(val);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        out.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @RequiredArgsConstructor
    private static final class Decorator extends OutputStream {

        private final DataOutput out;

        @Override
        public void write(int val) throws IOException {
            out.writeByte(val);
        }

        @Override
        public void write(byte[] buf, int offs, int len) throws IOException {
            out.write(buf, offs, len);
        }

    }

}
