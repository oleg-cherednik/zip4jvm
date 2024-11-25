package ru.olegcherednik.zip4jvm.io.in.file.consecutive;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.in.BaseConsecutiveAccessDataInput;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.ByteUtils;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import lombok.Getter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Oleg Cherednik
 * @since 25.11.2024
 */
public class SolidSequentialAccessDataInput extends BaseConsecutiveAccessDataInput {

    private static final int READ_LIMIT = ByteUtils.DWORD_SIZE;

    private final SrcZip srcZip;
    private final BufferedInputStream in;
    @Getter
    private long absOffs;
    private long markOffs;

    public SolidSequentialAccessDataInput(SrcZip srcZip) throws IOException {
        this.srcZip = srcZip;
        in = new BufferedInputStream(Files.newInputStream(srcZip.getDiskByNo(0).getPath()));
    }

    // ---------- DataInput ----------

    @Override
    public ByteOrder getByteOrder() {
        return srcZip.getByteOrder();
    }

    @Override
    public long skip(long bytes) throws IOException {
        long skipped = in.skip(bytes);
        absOffs += skipped;
        return skipped;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int readNow = in.read(buf, offs, len);

        if (readNow > 0)
            absOffs += readNow;

        return readNow;
    }

    // ---------- ConsecutiveAccessDataInput ----------

    @Override
    public void mark() {
        markOffs = absOffs;
        in.mark(READ_LIMIT);
    }

    @Override
    public void markReset() throws IOException {
        absOffs = markOffs;
        in.reset();
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        in.close();
        super.close();
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return in == null ? "<empty>" : PathUtils.getOffsStr(getAbsOffs());
    }

}
