package ru.olegcherednik.zip4jvm.model.builders;

import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 30.08.2019
 */
@RequiredArgsConstructor
final class AesExtraDataRecordBuilder {

    @NonNull
    private final ZipEntry entry;

    @NonNull
    public AesExtraDataRecord create() {
        AesStrength strength = entry.getStrength();

        if (strength == AesStrength.NULL)
            return AesExtraDataRecord.NULL;

        return AesExtraDataRecord.builder()
                                 .size(7)
                                 .vendor("AE")
                                 .versionNumber((short)2)
                                 .strength(strength)
                                 .compressionMethod(entry.getCompression().getMethod())
                                 .build();
    }

}
