package ru.olegcherednik.zip4jvm.io.zstd;

import lombok.RequiredArgsConstructor;
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

    private final com.github.luben.zstd.ZstdInputStream in;
    private final byte[] buf = new byte[1];
    private final DataInput dataInput;
    private final long finalAbsoluteOffs;
    private long bytesToRead;

    public ZstdInputStream(DataInput in, long uncompressedSize, long compressedSize) throws IOException {
        this.in = new com.github.luben.zstd.ZstdInputStream(new Decorator(in));
        dataInput = in;
        finalAbsoluteOffs = dataInput.getAbsoluteOffs() + compressedSize;
        bytesToRead = uncompressedSize;
    }

    @Override
    public int read() throws IOException {
        return read(buf, 0, 1) == -1 ? -1 : (buf[0] & 0xFF);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        if (bytesToRead <= 0) {
            dataInput.seek(finalAbsoluteOffs);
            return IOUtils.EOF;
        }

        int total = in.read(buf, offs, len);
        bytesToRead -= total;
        return total;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @RequiredArgsConstructor
    private static final class Decorator extends InputStream {

        private final DataInput in;

        @Override
        public int read() throws IOException {
            return in.readByte();
        }

        @Override
        public int read(byte[] buf, int offs, int len) throws IOException {
            return in.read(buf, offs, len);
        }

        @Override
        public void close() throws IOException {
            in.close();
        }

    }

}


