package ru.olegcherednik.zip4jvm.io.out.data.decorators;

import ru.olegcherednik.zip4jvm.io.out.data.ByteOrderConverter;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.11.2024
 */
public class ByteOrderDataOutput extends XxxByteOrderDataOutputDecorator {

    private final ByteOrderConverter byteOrderConverter;

    protected ByteOrderDataOutput(DataOutput delegate) {
        super(delegate);
        byteOrderConverter = new ByteOrderConverter(delegate.getByteOrder());
    }

    // ---------- DataOutput ----------

    @Override
    public void writeByte(int val) throws IOException {
        byteOrderConverter.writeByte(val, this);
    }

    @Override
    public void writeWord(int val) throws IOException {
        byteOrderConverter.writeWord(val, this);
    }

    @Override
    public void writeDword(long val) throws IOException {
        byteOrderConverter.writeDword(val, this);
    }

    @Override
    public void writeQword(long val) throws IOException {
        byteOrderConverter.writeQword(val, this);
    }

}
