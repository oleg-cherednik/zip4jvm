package ru.olegcherednik.zip4jvm.io.out.data;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.utils.BitUtils;

import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 02.11.2024
 */
@RequiredArgsConstructor
public class Converter {

    private final ByteOrder byteOrder;

    public void writeByte(int val, DataOutput out) throws IOException {
        out.write((byte) val);
    }

    public void writeWord(int val, DataOutput out) throws IOException {
        val = byteOrder.convertWord(val);

        for (int i = 0; i < 2; i++)
            out.write(BitUtils.getByte(val, i));
    }

    public void writeDword(long val, DataOutput out) throws IOException {
        val = byteOrder.convertDword(val);

        for (int i = 0; i < 4; i++)
            out.write(BitUtils.getByte(val, i));
    }

    public void writeQword(long val, DataOutput out) throws IOException {
        val = byteOrder.convertQword(val);

        for (int i = 0; i < 8; i++)
            out.write(BitUtils.getByte(val, i));
    }

}
