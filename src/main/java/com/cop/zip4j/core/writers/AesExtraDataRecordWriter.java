package com.cop.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.cop.zip4j.io.SplitOutputStream;
import com.cop.zip4j.model.aes.AesExtraDataRecord;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 15.04.2019
 */
@RequiredArgsConstructor
final class AesExtraDataRecordWriter {

    @NonNull
    private final AesExtraDataRecord record;
    @NonNull
    private final Charset charset;

    public void write(@NonNull SplitOutputStream out) throws IOException {
        if (record == AesExtraDataRecord.NULL)
            return;

        out.writeWord(AesExtraDataRecord.SIGNATURE);
        out.writeWord(record.getDataSize());
        out.writeWord(record.getVersionNumber());
        out.writeBytes(record.getVendor(charset));
        out.writeBytes((byte)record.getStrength().getRawCode(), (byte)0);
        out.writeWord(record.getCompressionMethod().getValue());
    }

}
