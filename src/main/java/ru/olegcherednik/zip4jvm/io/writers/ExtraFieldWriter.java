package ru.olegcherednik.zip4jvm.io.writers;

import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor
final class ExtraFieldWriter {

    private static final String MARK = ExtraFieldWriter.class.getSimpleName();

    @NonNull
    private final ExtraField extraField;
    @NonNull
    private final Charset charset;

    public void write(@NonNull DataOutput out) throws IOException {
        out.mark(MARK);

        new Zip64Writer.ExtendedInfo(extraField.getExtendedInfo()).write(out);
        new AesExtraDataRecordWriter(extraField.getAesExtraDataRecord(), charset).write(out);

        if(extraField.getSize() != out.getWrittenBytesAmount(MARK))
            throw new Zip4jException("Illegal number of written bytes");
    }

}
