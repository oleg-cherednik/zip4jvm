package ru.olegcherednik.zip4jvm.io.writers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 15.04.2019
 */
@RequiredArgsConstructor
final class AesExtraDataRecordWriter implements Writer {

    private final AesExtraDataRecord record;

    @Override
    public void write(DataOutput out) throws IOException {
        if (record == AesExtraDataRecord.NULL)
            return;

        out.writeWordSignature(AesExtraDataRecord.SIGNATURE);
        out.writeWord(record.getSize());
        out.writeWord(record.getVendorVersion());
        out.writeBytes(record.getVendor(Charsets.UTF_8));
        out.writeBytes((byte)record.getStrength().getCode());
        out.writeWord(record.getCompressionMethod().getCode());
    }

}
