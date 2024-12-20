package ru.olegcherednik.zip4jvm.io.in.file.consecutive;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import lombok.Getter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireZeroOrPositive;

/**
 * @author Oleg Cherednik
 * @since 25.11.2024
 */
public class SolidConsecutiveAccessDataInput extends BaseConsecutiveAccessDataInput {

    @Getter
    private final ByteOrder byteOrder;
    private final InputStream in;

    public SolidConsecutiveAccessDataInput(SrcZip srcZip) throws IOException {
        byteOrder = srcZip.getByteOrder();
        in = new BufferedInputStream(Files.newInputStream(srcZip.getDiskByNo(0).getPath()));
    }

    // ---------- DataInput ----------

    @Override
    public long skip(long bytes) throws IOException {
        requireZeroOrPositive(bytes, "skip.bytes");

        long skipped = in.skip(bytes);
        incAbsOffs(skipped);
        return skipped;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int readNow = in.read(buf, offs, len);

        if (readNow > 0)
            incAbsOffs(readNow);

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
        return PathUtils.getOffsStr(getAbsOffs());
    }

}
