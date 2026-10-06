package io.airlift.compress.zstd;

import ru.olegcherednik.zip4jvm.io.in.file.random.ByteArrayDataInput;

import static io.airlift.compress.zstd.Constants.SIZE_OF_BYTE;
import static io.airlift.compress.zstd.Constants.SIZE_OF_INT;
import static io.airlift.compress.zstd.Constants.SIZE_OF_LONG;
import static io.airlift.compress.zstd.Constants.SIZE_OF_SHORT;

/**
 * @author Oleg Cherednik
 * @since 04.10.2026
 */
public class InputStreamForRead extends ReadByteArrayWithOffs {

    private final ByteArrayDataInput in;

    public InputStreamForRead(byte[] buf) {
        super(buf);
        in = new ByteArrayDataInput(buf);
    }

    @Override
    public int available() {
        return (int) in.available();
    }

    @Override
    public int getOffs() {
        return (int) in.getAbsOffs();
    }

    @Override
    public int getByte() {
        int res = in.readByte();
        offs += SIZE_OF_BYTE;
        return res;
    }

    @Override
    public int getShort() {
        int res = in.readWord();
        offs += SIZE_OF_SHORT;
        return res;
    }

    @Override
    public int getInt() {
        int res = (int) in.readDword();
        offs += SIZE_OF_INT;
        return res;
    }

    @Override
    public long getLong() {
        long res = in.readDword();
        offs += SIZE_OF_LONG;
        return res;
    }

    @Override
    public void copyMemory(byte[] out, int bytes) {
        in.read(out, 0, bytes);
        offs += bytes;
    }

    @Override
    public void copyMemory(byte[] out, int outOffs, int bytes) {
        in.read(out, outOffs, bytes);
        offs += bytes;
    }

    @Override
    public byte[] readBytes(int total) {
        byte[] buf = in.readBytes(total);
        offs += total;
        return buf;
    }

}
