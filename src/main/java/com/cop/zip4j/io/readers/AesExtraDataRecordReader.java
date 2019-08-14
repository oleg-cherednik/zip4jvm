package com.cop.zip4j.io.readers;

import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.CompressionMethod;
import com.cop.zip4j.model.aes.AesExtraDataRecord;
import com.cop.zip4j.model.aes.AesStrength;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor
final class AesExtraDataRecordReader {

    private final int signature;

    @NonNull
    public AesExtraDataRecord read(@NonNull DataInput in) throws IOException {
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
