package ru.olegcherednik.zip4jvm.io.out.entry;

import ru.olegcherednik.zip4jvm.io.out.entry.encrypted.EncryptedEntryOutputStream;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * @author Oleg Cherednik
 * @since 28.10.2024
 */
@RequiredArgsConstructor
public class FooOutputStream extends OutputStream {

    private final EncryptedEntryOutputStream eos;
    private final Checksum checksum = new CRC32();

    private long uncompressedSize;

    @Override
    public final void write(int b) throws IOException {
        checksum.update(b);
        uncompressedSize++;
        eos.write(b);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        checksum.update(buf, offs, len);
        uncompressedSize += Math.max(0, len);
        eos.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        eos.close();
    }

    @Override
    public String toString() {
        return eos.toString();
    }

}
