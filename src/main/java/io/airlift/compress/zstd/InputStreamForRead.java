package io.airlift.compress.zstd;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.file.random.ByteArrayDataInput;

import static io.airlift.compress.zstd.Constants.SIZE_OF_BYTE;
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
    public int getByte() {
        in.offs = offs;
        int res = in.readByte();
        offs += SIZE_OF_BYTE;
        return res;
    }

    @Override
    public int getShort() {
        in.offs = offs;
        int res = in.readWord();
        offs += SIZE_OF_SHORT;
        return res;
    }

//    @Override
//    public int getInt() {
//        int val = super.getInt();
//        int val1 = (int) in.readDword();
////        in.skip(SIZE_OF_INT);
//        return val;
//    }
//
//    @Override
//    public long getLong() {
//        long val = super.getLong();
//        long val1 = in.readDword();
////        in.skip(SIZE_OF_LONG);
//        return val;
//    }
//
//    @Override
//    public void copyMemory(int inOffs, byte[] out, int outOffs, int bytes) {
//        super.copyMemory(inOffs, out, outOffs, bytes);
//        in.skip(bytes);
//    }
//
//    public void copyMemory(byte[] out, int bytes) {
//        System.arraycopy(buf, offs, out, 0, bytes);
//        in.skip(bytes);
//    }
//
//    @Override
//    public void copyMemory(ByteArrayWithOffs out, int bytes) {
//        super.copyMemory(out, bytes);
//        in.skip(bytes);
//    }
//
//    @Override
//    public byte[] readBytes(int total) {
//        byte[] buf = super.readBytes(total);
//        in.skip(total);
//        return buf;
//    }

}
