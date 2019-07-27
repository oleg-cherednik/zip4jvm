package com.cop.zip4j.crypto.pkware;

import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.io.SplitOutputStream;
import lombok.NonNull;

import java.io.IOException;
import java.util.Random;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public final class StandardEncoder implements Encoder {

    public static final int SIZE_HEADER = 12;

    private final StandardEngine standardEngine;
    private final byte[] header;

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public StandardEncoder(@NonNull char[] password) {
        standardEngine = new StandardEngine(password);
        header = createRandomHeader();
    }

    private byte[] createRandomHeader() {
        byte[] header = new byte[SIZE_HEADER];
        new Random().nextBytes(header);
        encode(header, 0, header.length);
        return header;
    }

    @Override
    public void encode(@NonNull byte[] buf, int offs, int len) {
        for (int i = offs; i < offs + len; i++)
            buf[i] = standardEngine.encode(buf[i]);
    }

    @Override
    public void write(@NonNull SplitOutputStream out) throws IOException {
        out.writeBytes(header);
    }

}
