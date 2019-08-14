package com.cop.zip4j.io.writers;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.ExtraField;
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

    @NonNull
    private final ExtraField extraField;
    @NonNull
    private final Charset charset;

    public void write(@NonNull DataOutput out) throws IOException {
        new Zip64Writer.ExtendedInfoWriter(extraField.getExtendedInfo()).write(out);
        new AesExtraDataRecordWriter(extraField.getAesExtraDataRecord(), charset).write(out);
    }

}
