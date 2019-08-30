package com.cop.zip4j.model.builders;

import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.aes.AesExtraDataRecord;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 30.08.2019
 */
@RequiredArgsConstructor
final class AesExtraDataRecordBuilder {

    @NonNull
    private final PathZipEntry entry;

    public AesExtraDataRecord create() {
        if (entry.getEncryption() != Encryption.AES)
            return AesExtraDataRecord.NULL;

        return AesExtraDataRecord.builder()
                                 .size(7)
                                 .vendor("AE")
                                 .versionNumber((short)2)
                                 .strength(entry.getStrength())
                                 .compressionMethod(entry.getCompression().getMethod())
                                 .build();
    }

}
