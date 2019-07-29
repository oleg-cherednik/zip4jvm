package com.cop.zip4j.crypto.pkware;

import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.io.SplitOutputStream;
import com.cop.zip4j.utils.ZipUtils;
import lombok.NonNull;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public final class StandardEncoder implements Encoder {

    public static final int SIZE_RND_HEADER = 12;

    private final StandardEngine standardEngine;
    private final byte[] headerBytes = new byte[SIZE_RND_HEADER];

    public StandardEncoder(@NonNull char[] password, int crc) {
        standardEngine = new StandardEngine(password);
        init(crc);
    }

    private void init(int crc) {
        headerBytes[SIZE_RND_HEADER - 1] = (byte)(crc >>> 24);
        headerBytes[SIZE_RND_HEADER - 2] = (byte)(crc >>> 16);
        encode(headerBytes);
    }

    void encode(byte[] buf) {
        encode(buf, 0, buf.length);
    }

    @Override
    public void encode(@NonNull byte[] buf, int offs, int len) {
        ZipUtils.checkEquealOrGreaterZero(offs);
        ZipUtils.checkEquealOrGreaterZero(len);

        for (int i = offs; i < offs + len; i++)
            buf[i] = standardEngine.encode(buf[i]);
    }

    @Override
    public void write(@NonNull SplitOutputStream out) throws IOException {
        out.writeBytes(headerBytes);
    }

}
