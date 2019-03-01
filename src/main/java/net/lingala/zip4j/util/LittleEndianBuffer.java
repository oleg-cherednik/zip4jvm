package net.lingala.zip4j.util;

import lombok.NonNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 01.03.2019
 */
public final class LittleEndianBuffer implements Closeable {
    private final List<String> bytes = new ArrayList<>();

    public int flushInto(OutputStream out) throws IOException {
        byte[] lhBytes = byteArrayListToByteArray();
        out.write(lhBytes);
        return lhBytes.length;
    }

    public void copyByteArrayToArrayList(@NonNull byte[] buf) {
        for (int i = 0; i < buf.length; i++)
            bytes.add(Byte.toString(buf[i]));
    }

    public byte[] byteArrayListToByteArray() {
        byte[] retBytes = new byte[bytes.size()];

        for (int i = 0; i < bytes.size(); i++)
            retBytes[i] = Byte.parseByte(bytes.get(i));

        return retBytes;
    }

    // ---------- Closeable ----------

    @Override
    public void close() throws IOException {

    }

}
