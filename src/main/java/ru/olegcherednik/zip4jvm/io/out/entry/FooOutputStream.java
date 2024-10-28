package ru.olegcherednik.zip4jvm.io.out.entry;

import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.PureJavaCrc32;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Checksum;

/**
 * @author Oleg Cherednik
 * @since 28.10.2024
 */
@RequiredArgsConstructor
public class FooOutputStream extends OutputStream {

    private final ZipEntry zipEntry;
    private final OutputStream out;
    private final Checksum checksum = new PureJavaCrc32();

    private long uncompressedSize;

    @Override
    public final void write(int b) throws IOException {
        checksum.update(b);
        uncompressedSize++;
        out.write(b);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        checksum.update(buf, offs, len);
        uncompressedSize += Math.max(0, len);
        out.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        zipEntry.setChecksum(checksum.getValue());
        zipEntry.setUncompressedSize(uncompressedSize);
        out.close();
    }

    @Override
    public String toString() {
        return out.toString();
    }

}
