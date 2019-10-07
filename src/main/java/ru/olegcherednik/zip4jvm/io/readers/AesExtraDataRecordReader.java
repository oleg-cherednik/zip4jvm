package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor
final class AesExtraDataRecordReader implements Reader<AesExtraDataRecord> {

    private final int signature;

    @Override
    public AesExtraDataRecord read(DataInput in) throws IOException {
        if (signature != AesExtraDataRecord.SIGNATURE)
            return AesExtraDataRecord.NULL;

        int size = in.readWord();
        int versionNumber = in.readWord();
        String vendor = in.readString(2, Charsets.UTF_8);
        AesStrength strength = AesStrength.parseValue(in.readByte());
        CompressionMethod compressionMethod = CompressionMethod.parseCode(in.readWord());

        return AesExtraDataRecord.builder()
                                 .size(size)
                                 .versionNumber(versionNumber)
                                 .vendor(vendor)
                                 .strength(strength)
                                 .compressionMethod(compressionMethod).build();
    }
}
