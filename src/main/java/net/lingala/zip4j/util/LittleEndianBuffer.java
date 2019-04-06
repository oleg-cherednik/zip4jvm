package net.lingala.zip4j.util;

import lombok.NonNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 01.03.2019
 */
public final class LittleEndianBuffer implements Closeable {
    private final List<String> bytes = new ArrayList<>();

    public void copyByteArrayToArrayList(@NonNull byte[] buf) {
        for (int i = 0; i < buf.length; i++)
            bytes.add(Byte.toString(buf[i]));
    }

    public byte[] byteArrayListToByteArray() {
        byte[] buf = new byte[bytes.size()];

        for (int i = 0; i < bytes.size(); i++)
            buf[i] = Byte.parseByte(bytes.get(i));

        return buf;
    }

    // ---------- Closeable ----------

    @Override
    public void close() throws IOException {

    }

}
