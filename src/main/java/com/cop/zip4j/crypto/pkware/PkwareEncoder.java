package com.cop.zip4j.crypto.pkware;

import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.exception.ZipException;
import com.cop.zip4j.io.SplitOutputStream;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.NonNull;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public class PkwareEncoder implements Encoder {

    public static final int SIZE_HEADER = 12;

    private final PkwareEngine engine;
    private final byte[] header;

    public static PkwareEncoder create(@NonNull LocalFileHeader localFileHeader, @NonNull PathZipEntry entry) {
        if (ArrayUtils.isEmpty(entry.getPassword()))
            throw new ZipException("Passwords should not be empty for '" + Encryption.PKWARE.name() + "' encryption");
        // Since we do not know the crc here, we use the modification time for encrypting.
        return new PkwareEncoder(entry.getPassword(), (localFileHeader.getLastModifiedTime() & 0xFFFF) << 16);
    }

    public PkwareEncoder(@NonNull char[] password, int crc32) {
        engine = new PkwareEngine(password);
        header = createHeader(crc32, engine);
    }

    @Override
    public void encrypt(@NonNull byte[] buf, int offs, int len) {
        encrypt(buf, offs, len, engine);
    }

    @Override
    public void write(@NonNull SplitOutputStream out) throws IOException {
        out.writeBytes(header);
    }

    private static byte[] createHeader(int crc32, PkwareEngine engine) {
        byte[] header = new byte[SIZE_HEADER];
        header[header.length - 1] = (byte)(crc32 >>> 24);
        header[header.length - 2] = (byte)(crc32 >>> 16);
        encrypt(header, 0, header.length, engine);
        return header;
    }

    private static void encrypt(byte[] buf, int offs, int len, PkwareEngine engine) {
        for (int i = offs; i < offs + len; i++)
            buf[i] = engine.encrypt(buf[i]);
    }

}
