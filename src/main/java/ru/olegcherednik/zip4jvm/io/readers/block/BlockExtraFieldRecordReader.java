package ru.olegcherednik.zip4jvm.io.readers.block;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.ExtraFieldRecordReader;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldListBlock;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
public class BlockExtraFieldRecordReader extends ExtraFieldRecordReader {

    private final ExtraFieldListBlock extraField;

    public BlockExtraFieldRecordReader(Map<Integer, Function<Integer, Reader<? extends ExtraField.Record>>> readers, ExtraFieldListBlock extraField) {
        super(readers);
        this.extraField = extraField;
    }

    @Override
    public ExtraField.Record read(DataInput in) throws IOException {
        extraField.addRecord();

        ExtraField.Record record = extraField.getRecord().calc(in, () -> super.read(in));
        extraField.saveRecord(record.getSignature());

        return record;
    }
}
