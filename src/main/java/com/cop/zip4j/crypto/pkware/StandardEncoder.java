package com.cop.zip4j.crypto.pkware;

import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.io.SplitOutputStream;
import com.cop.zip4j.utils.ZipUtils;
import lombok.NonNull;

import java.io.IOException;
import java.util.Random;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public final class StandardEncoder implements Encoder {

    public static final int SIZE_RND_HEADER = 12;

    private final StandardEngine standardEngine;
    private final byte[] header;

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public StandardEncoder(@NonNull char[] password) {
        standardEngine = new StandardEngine(password);
        header = createRandomHeader();
    }

    private byte[] createRandomHeader() {
        byte[] header = new byte[SIZE_RND_HEADER];
        new Random().nextBytes(header);

        for (int i = 0; i < header.length; i++)
            header[i] = standardEngine.encode(header[i]);

        return header;
    }

//    private void init(int crc) {
//        header[SIZE_RND_HEADER - 1] = (byte)(crc >>> 24);
//        header[SIZE_RND_HEADER - 2] = (byte)(crc >>> 16);
//        encode(header);
//    }

    @Override
    public void encode(@NonNull byte[] buf, int offs, int len) {
        ZipUtils.checkEquealOrGreaterZero(offs);
        ZipUtils.checkEquealOrGreaterZero(len);

        for (int i = offs; i < offs + len; i++)
            buf[i] = standardEngine.encode(buf[i]);
    }

    @Override
    public void write(@NonNull SplitOutputStream out) throws IOException {
        out.writeBytes(header);
    }

}
