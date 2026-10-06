package io.airlift.compress.zstd;

import ru.olegcherednik.zip4jvm.io.in.file.random.ByteArrayDataInput;

/**
 * @author Oleg Cherednik
 * @since 04.10.2026
 */
public class InputStreamForRead {

    private final ByteArrayDataInput in;

    public InputStreamForRead(byte[] buf) {
        in = new ByteArrayDataInput(buf);
    }

    public int available() {
        return (int) in.available();
    }

    public int getOffs() {
        return (int) in.getAbsOffs();
    }

    public int getByte() {
        return in.readByte();
    }

    public int getShort() {
        return in.readWord();
    }

    public int getInt() {
        return (int) in.readDword();
    }

    public long getLong() {
        return in.readDword();
    }

    public void copyMemory(byte[] out, int outOffs, int bytes) {
        in.read(out, outOffs, bytes);
    }

    public byte[] readBytes(int total) {
        return in.readBytes(total);
    }

}
