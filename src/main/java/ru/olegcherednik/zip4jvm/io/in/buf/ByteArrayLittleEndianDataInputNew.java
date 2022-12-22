package ru.olegcherednik.zip4jvm.io.in.buf;

import ru.olegcherednik.zip4jvm.io.in.file.LittleEndianDataInputFile;

/**
 * @author Oleg Cherednik
 * @since 09.12.2022
 */
public class ByteArrayLittleEndianDataInputNew extends ByteArrayDataInputNew {

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public ByteArrayLittleEndianDataInputNew(byte[] src) {
        super(src);
    }

    @Override
    public long toLong(byte[] buf, int offs, int len) {
        return LittleEndianDataInputFile.getLong(buf, offs, len);
    }

}
