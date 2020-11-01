package ru.olegcherednik.zip4jvm.io.readers.extrafiled;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.extrafield.ExecutableJarMarkerExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 11.04.2020
 */
@RequiredArgsConstructor
public final class ExecutableJarMarkerExtraFieldRecordReader implements Reader<ExecutableJarMarkerExtraFieldRecord> {

    private final int size;

    @Override
    public ExecutableJarMarkerExtraFieldRecord read(DataInput in) throws IOException {
        return new ExecutableJarMarkerExtraFieldRecord(size);
    }

}
