package io.airlift.compress.zstd;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.file.random.ByteArrayDataInput;

/**
 * @author Oleg Cherednik
 * @since 04.10.2026
 */
public class InputStreamForRead {

    private final DataInput in;

    public InputStreamForRead(byte[] buf) {
        in = new ByteArrayDataInput(buf);
    }

    public long available() {
        return in.available();
    }

    public long getAbsOffs() {
        return in.getAbsOffs();
    }

    public int readByte() {
        return in.readByte();
    }

    public int readWord() {
        return in.readWord();
    }

    public int readDword() {
        return (int) in.readDword();
    }

    public long readQword() {
        return in.readQword();
    }

    public void read(byte[] out, int outOffs, int bytes) {
        in.read(out, outOffs, bytes);
    }

    public byte[] readBytes(int total) {
        return in.readBytes(total);
    }

}
