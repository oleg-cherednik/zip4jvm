package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.ZipModel;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 15.04.2019
 */
@RequiredArgsConstructor
final class AESExtraDataRecordWriter {

    private final AESExtraDataRecord record;
    @NonNull
    private final ZipModel zipModel;

    public void write(@NonNull OutputStreamDecorator out) throws IOException {
        if (record == null)
            return;

        out.writeWord((short)record.getSignature());
        out.writeWord((short)record.getDataSize());
        out.writeShort((short)record.getVersionNumber());
        out.writeBytes(record.getVendor().getBytes(zipModel.getCharset()));
        out.writeBytes(record.getAesStrength().getValue(), (byte)0);
        out.writeWord(record.getCompressionMethod().getValue());
    }

}
