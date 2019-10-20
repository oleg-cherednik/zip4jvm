package ru.olegcherednik.zip4jvm.io.readers.block;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.ExtraFieldReader;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
public class BlockExtraFieldReader extends ExtraFieldReader {

    public BlockExtraFieldReader(int size, Map<Integer, Function<Integer, Reader<? extends ExtraField.Record>>> readers) {
        super(size, new BlockExtraFieldRecordReader(readers));
    }

    @Override
    protected ExtraField readExtraField(DataInput in) throws IOException {
        Diagnostic.CentralDirectory.FileHeader fileHeader = Diagnostic.getInstance().getCentralDirectory().getFileHeader();
        fileHeader.addExtraField();
        return fileHeader.getExtraField().calc(in, () -> super.readExtraField(in));
    }

}
