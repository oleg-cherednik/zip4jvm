package ru.olegcherednik.zip4jvm.io.readers.diagnostic;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.diagnostic.Block;
import ru.olegcherednik.zip4jvm.model.diagnostic.Diagnostic;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
public class ExtraFieldReaderB extends ExtraFieldReaderA {

    public ExtraFieldReaderB(int size, Map<Integer, Function<Integer, Reader<? extends ExtraField.Record>>> readers) {
        super(size, new ExtraFieldRecordReaderB(readers));
    }

    @Override
    protected ExtraField readExtraField(DataInput in) throws IOException {
        Diagnostic.CentralDirectory.FileHeader fileHeader = Diagnostic.getInstance().getCentralDirectory().getFileHeader();
        fileHeader.addExtraField();
        return Block.foo(in, fileHeader.getExtraField(), () -> super.readExtraField(in));
    }

}
