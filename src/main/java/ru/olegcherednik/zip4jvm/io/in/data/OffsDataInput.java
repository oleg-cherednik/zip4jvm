package ru.olegcherednik.zip4jvm.io.in.data;

import ru.olegcherednik.zip4jvm.utils.BitUtils;

import lombok.Getter;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.11.2024
 */
@Getter
public class OffsDataInput extends BaseDataInput {

    private long absOffs;

    public OffsDataInput(DataInput in) {
        super(in);
    }

    // ---------- DataInput ----------

    @Override
    public int readByte() throws IOException {
        absOffs += BitUtils.BYTE_SIZE;
        return super.readByte();
    }

    @Override
    public int readWord() throws IOException {
        absOffs += BitUtils.WORD_SIZE;
        return super.readWord();
    }

    @Override
    public long readDword() throws IOException {
        absOffs += BitUtils.DWORD_SIZE;
        return super.readDword();
    }

    @Override
    public long readQword() throws IOException {
        absOffs += BitUtils.QWORD_SIZE;
        return super.readQword();
    }

    @Override
    public long skip(long bytes) throws IOException {
        long b = in.skip(bytes);
        absOffs += b;
        return b;
    }

    // ---------- ReadBuffer ----------

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int readNow = in.read(buf, offs, len);

        if (readNow != IOUtils.EOF)
            absOffs += readNow;

        return readNow;
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return String.format("offs: %s (0x%s)", absOffs, Long.toHexString(absOffs));
    }

}
