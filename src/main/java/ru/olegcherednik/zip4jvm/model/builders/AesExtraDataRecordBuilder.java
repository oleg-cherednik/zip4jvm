package ru.olegcherednik.zip4jvm.model.builders;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.aes.AesEngine;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.extrafield.AesExtraFieldRecord;

/**
 * @author Oleg Cherednik
 * @since 30.08.2019
 */
@RequiredArgsConstructor
final class AesExtraDataRecordBuilder {

    private final ZipEntry zipEntry;

    public AesExtraFieldRecord build() {
        AesStrength strength = AesEngine.getStrength(zipEntry.getEncryptionMethod());

        if (strength == AesStrength.NULL)
            return AesExtraFieldRecord.NULL;

        return AesExtraFieldRecord.builder()
                                  .dataSize(7)
                                  .vendor("AE")
                                  .versionNumber(2)
                                  .strength(strength)
                                  .compressionMethod(zipEntry.getCompressionMethod()).build();
    }

}
