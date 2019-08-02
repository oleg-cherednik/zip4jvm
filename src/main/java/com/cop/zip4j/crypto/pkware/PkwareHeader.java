package com.cop.zip4j.crypto.pkware;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.DataOutputStream;
import com.cop.zip4j.io.LittleEndianRandomAccessFile;
import com.cop.zip4j.model.LocalFileHeader;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Random;

/**
 * @author Oleg Cherednik
 * @since 29.07.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class PkwareHeader {

    public static final int SIZE = 12;

    private final byte[] buf;

    public static PkwareHeader create(@NonNull LocalFileHeader localFileHeader, @NonNull PkwareEngine engine) {
        // TODO Instead of CRC32, time should be used along with {@link GeneralPurposeFlag} bit 3 should be true
        int crc = getCrc(localFileHeader);
        return new PkwareHeader(create(engine, crc));
    }

    public static PkwareHeader read(@NonNull LittleEndianRandomAccessFile in, @NonNull LocalFileHeader localFileHeader, @NonNull PkwareEngine engine)
            throws IOException {
        in.seek(localFileHeader.getOffs());
        PkwareHeader header = new PkwareHeader(in.readBytes(SIZE));
        header.requireMatchCrc(localFileHeader, engine);
        return header;
    }

    public void write(@NonNull DataOutputStream out) throws IOException {
        out.writeBytes(buf);
    }

    private void requireMatchCrc(LocalFileHeader localFileHeader, PkwareEngine engine) {
        engine.decrypt(buf, 0, buf.length);
        int crc = getCrc(localFileHeader);

        if (buf[buf.length - 1] != low(crc) || buf[buf.length - 2] != high(crc))
            throw new Zip4jException("The specified password is incorrect");
    }

    private static byte[] create(PkwareEngine engine, int crc) {
        byte[] buf = new byte[SIZE];

        new Random().nextBytes(buf);
        buf[buf.length - 1] = low(crc);
        buf[buf.length - 2] = high(crc);
        engine.encrypt(buf, 0, buf.length);

        return buf;
    }

    private static int getCrc(LocalFileHeader localFileHeader) {
        return localFileHeader.getLastModifiedTime();
    }

    private static byte low(int crc) {
        return (byte)(crc >> 8);
    }

    private static byte high(int crc) {
        return (byte)crc;
    }

}
