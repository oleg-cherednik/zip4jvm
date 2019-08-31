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

        return AesExtraDataRecord.builder()
                                 .size(in.readWord())
                                 .versionNumber(in.readWord())
                                 .vendor(in.readString(2))
                                 .strength(AesStrength.parseValue(in.readByte()))
                                 .compressionMethod(CompressionMethod.parseCode(in.readWord()))
                                 .build();
    }
}
