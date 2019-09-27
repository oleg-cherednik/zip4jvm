package ru.olegcherednik.zip4jvm.io.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor
final class ExtraFieldWriter implements Writer {

    private static final String MARK = ExtraFieldWriter.class.getSimpleName();

    @NonNull
    private final ExtraField extraField;

    @Override
    public void write(@NonNull DataOutput out) throws IOException {
        out.mark(MARK);

        for (ExtraField.Record record : extraField.getRecords()) {
            if (record instanceof Zip64.ExtendedInfo)
                new Zip64Writer.ExtendedInfo((Zip64.ExtendedInfo)record).write(out);
            else if (record instanceof AesExtraDataRecord)
                new AesExtraDataRecordWriter((AesExtraDataRecord)record).write(out);
            else if (record instanceof ExtraField.Record.Unknown)
                new UnknownExtraFileRecordWriter((ExtraField.Record.Unknown)record).write(out);
        }

        if (extraField.getSize() != out.getWrittenBytesAmount(MARK))
            throw new Zip4jvmException("Illegal number of written bytes");
    }

    @RequiredArgsConstructor
    private static final class UnknownExtraFileRecordWriter implements Writer {

        private final ExtraField.Record.Unknown extraField;

        @Override
        public void write(@NonNull DataOutput out) throws IOException {
            out.writeWordSignature(Zip64.ExtendedInfo.SIGNATURE);
            out.writeWord(extraField.getBlockSize());
            out.write(extraField.getData(), 0, extraField.getBlockSize());
        }

    }

}
