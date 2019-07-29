package com.cop.zip4j.crypto.pkware;

import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.io.SplitOutputStream;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public class PkwareEncoder implements Encoder {

    public static final int SIZE_HEADER = 12;

    private final PkwareEngine engine;
    private final byte[] header;

    public PkwareEncoder(@NonNull char[] password, int crc32) {
        engine = new PkwareEngine(password);
        header = createHeader(crc32, engine);
    }

    @Override
    public void encrypt(@NonNull byte[] buf, int offs, int len) {
        encode(buf, offs, len, engine);
    }

    @Override
    public void write(@NonNull SplitOutputStream out) throws IOException {
        out.writeBytes(header);
    }

    private static byte[] createHeader(int crc32, PkwareEngine engine) {
        byte[] header = new byte[SIZE_HEADER];
        header[header.length - 1] = (byte)(crc32 >>> 24);
        header[header.length - 2] = (byte)(crc32 >>> 16);
        encode(header, 0, header.length, engine);
        return header;
    }

    private static void encode(byte[] buf, int offs, int len, PkwareEngine engine) {
        for (int i = offs; i < offs + len; i++)
            buf[i] = engine.encrypt(buf[i]);
    }

}
