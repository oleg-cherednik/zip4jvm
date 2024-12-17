package ru.olegcherednik.zip4jvm.io.in.file.consecutive;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.in.BaseConsecutiveAccessDataInput;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import lombok.Getter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * @author Oleg Cherednik
 * @since 25.11.2024
 */
public class SolidSequentialAccessDataInput extends BaseConsecutiveAccessDataInput {

    private final SrcZip srcZip;
    private final InputStream in;
    @Getter
    private long absOffs;

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
