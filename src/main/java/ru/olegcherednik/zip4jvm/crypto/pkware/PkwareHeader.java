package ru.olegcherednik.zip4jvm.crypto.pkware;

import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Random;

/**
 * @author Oleg Cherednik
 * @since 29.07.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PkwareHeader {

    public static final int SIZE = 12;

    private final byte[] buf;

    public static PkwareHeader create(@NonNull PkwareEngine engine, int checksum) {
        // TODO Instead of CRC32, time should be used along with {@link GeneralPurposeFlag} bit 3 should be true
        return new PkwareHeader(createBuf(engine, checksum));
    }

    private static byte[] createBuf(PkwareEngine engine, int checksum) {
        byte[] buf = new byte[SIZE];

        new Random().nextBytes(buf);
        buf[buf.length - 1] = low(checksum);
        buf[buf.length - 2] = high(checksum);
        engine.encrypt(buf, 0, buf.length);

        return buf;
    }

    public static PkwareHeader read(@NonNull PkwareEngine engine, @NonNull ZipEntry zipEntry, @NonNull DataInput in) throws IOException {
        PkwareHeader header = new PkwareHeader(in.readBytes(SIZE));
        header.requireMatchChecksum(engine, zipEntry);
        return header;
    }

    public void write(@NonNull DataOutput out) throws IOException {
        out.writeBytes(buf);
    }

    private void requireMatchChecksum(PkwareEngine engine, ZipEntry zipEntry) {
        engine.decrypt(buf, 0, buf.length);
        int checksum = getChecksum(zipEntry);

        if (buf[buf.length - 1] != low(checksum) || buf[buf.length - 2] != high(checksum))
            throw new IncorrectPasswordException(zipEntry.getFileName());
    }

    private static int getChecksum(ZipEntry zipEntry) {
        return zipEntry.getLastModifiedTime();
    }

    private static byte low(int checksum) {
        return (byte)(checksum >> 8);
    }

    private static byte high(int checksum) {
        return (byte)checksum;
    }

}
