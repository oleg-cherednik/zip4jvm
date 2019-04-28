package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.SplitOutputStream;
import net.lingala.zip4j.model.AESExtraDataRecord;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 15.04.2019
 */
@RequiredArgsConstructor
final class AESExtraDataRecordWriter {

    @NonNull
    private final AESExtraDataRecord record;
    @NonNull
    private final Charset charset;

    public void write(@NonNull SplitOutputStream out) throws IOException {
        if (record == AESExtraDataRecord.NULL)
            return;

        out.writeWord(AESExtraDataRecord.SIGNATURE);
        out.writeWord(record.getDataSize());
        out.writeWord(record.getVersionNumber());
        out.writeBytes(record.getVendor(charset));
        out.writeBytes(record.getAesStrength().getValue(), (byte)0);
        out.writeWord(record.getCompressionMethod().getValue());
    }

}
