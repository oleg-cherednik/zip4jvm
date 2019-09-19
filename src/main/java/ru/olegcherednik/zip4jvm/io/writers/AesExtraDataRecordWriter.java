package ru.olegcherednik.zip4jvm.io.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 15.04.2019
 */
@RequiredArgsConstructor
final class AesExtraDataRecordWriter implements Writer {

    @NonNull
    private final AesExtraDataRecord record;
    @NonNull
    private final Charset charset;

    @Override
    public void write(@NonNull DataOutput out) throws IOException {
        if (record == AesExtraDataRecord.NULL)
            return;

        out.writeWordSignature(AesExtraDataRecord.SIGNATURE);
        out.writeWord(record.getSize());
        out.writeWord(record.getVersionNumber());
        out.writeBytes(record.getVendor(charset));
        out.writeBytes((byte)record.getStrength().getCode());
        out.writeWord(record.getCompressionMethod().getCode());
    }

}
