package ru.olegcherednik.zip4jvm.io.readers.diagnostic;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.ExtraFieldRecordReader;
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
public class ExtraFieldRecordReaderB extends ExtraFieldRecordReader {

    public ExtraFieldRecordReaderB(Map<Integer, Function<Integer, Reader<? extends ExtraField.Record>>> readers) {
        super(readers);
    }

    @Override
    public ExtraField.Record read(DataInput in) throws IOException {
        Diagnostic.ExtraField extraField = Diagnostic.getInstance().getCentralDirectory().getFileHeader().getExtraField();
        extraField.addRecord();

        ExtraField.Record record = Block.foo(in, extraField.getRecord(), () -> super.read(in));
        extraField.saveRecord(record.getSignature());

        return record;
    }
}
