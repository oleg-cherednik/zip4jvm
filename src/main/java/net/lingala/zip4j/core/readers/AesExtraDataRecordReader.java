package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.LittleEndianRandomAccessFile;
import net.lingala.zip4j.model.AesExtraDataRecord;
import net.lingala.zip4j.model.AesStrength;
import net.lingala.zip4j.model.CompressionMethod;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor
final class AesExtraDataRecordReader {

    private final int signature;

    @NonNull
    public AesExtraDataRecord read(@NonNull LittleEndianRandomAccessFile in) throws IOException {
        if (signature != AesExtraDataRecord.SIGNATURE)
            return AesExtraDataRecord.NULL;

        AesExtraDataRecord record = new AesExtraDataRecord();
        record.setDataSize(in.readWord());
        record.setVersionNumber(in.readWord());
        record.setVendor(in.readString(2));
        record.setAesStrength(AesStrength.parseValue(in.readByte()));
        record.setCompressionMethod(CompressionMethod.parseValue(in.readWord()));
        return record;
    }
}
