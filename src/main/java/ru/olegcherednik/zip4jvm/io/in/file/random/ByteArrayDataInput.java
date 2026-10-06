package ru.olegcherednik.zip4jvm.io.in.file.random;

import ru.olegcherednik.zip4jvm.io.ByteOrder;

import lombok.Getter;

import java.util.Arrays;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireLessOrEqual;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireZeroOrPositive;

/**
 * @author Oleg Cherednik
 * @since 05.10.2026
 */
public class ByteArrayDataInput extends BaseRandomAccessDataInput {

    private final byte[] buf;
    @Getter
    private long absOffs;

    public ByteArrayDataInput(byte[] buf) {
        super(buf.length, ByteOrder.LITTLE_ENDIAN);
        this.buf = Arrays.copyOf(buf, buf.length);
    }

    // ---------- RandomAccessDataInput ----------

    @Override
    public void seek(long absOffs) {
        requireZeroOrPositive(absOffs, "seek.absOffs");
        requireLessOrEqual(absOffs, buf.length - 1, "seek.absOffs");

        this.absOffs = (int) absOffs;
    }

    // ---------- RandomAccessDataInput ----------

    @Override
    public long skip(long bytes) {
        int skipNow = (int) Math.min(bytes, available());
        absOffs += skipNow;
        return skipNow;
    }

    @Override
    public int read(byte[] buf, int offs, int len) {
        int readNow = (int) Math.min(len, available());
        System.arraycopy(this.buf, (int) absOffs, buf, offs, readNow);

        for (int i = 0; i < readNow; i++)
            buf[offs + i] = this.buf[(int) (absOffs + i)];

        absOffs += readNow;
        return readNow;
    }

}
