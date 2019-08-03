package com.cop.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.cop.zip4j.io.in.LittleEndianReadFile;
import com.cop.zip4j.model.aes.AesExtraDataRecord;
import com.cop.zip4j.model.aes.AesStrength;
import com.cop.zip4j.model.CompressionMethod;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor
final class AesExtraDataRecordReader {

    private final int signature;

    @NonNull
    public AesExtraDataRecord read(@NonNull LittleEndianReadFile in) throws IOException {
        if (signature != AesExtraDataRecord.SIGNATURE)
            return AesExtraDataRecord.NULL;

        AesExtraDataRecord record = new AesExtraDataRecord();
        record.setDataSize(in.readWord());
        record.setVersionNumber(in.readWord());
        record.setVendor(in.readString(2));
        record.setStrength(AesStrength.parseValue(in.readByte()));
        record.setCompressionMethod(CompressionMethod.parseValue(in.readWord()));
        return record;
    }
}
