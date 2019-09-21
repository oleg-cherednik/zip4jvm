package ru.olegcherednik.zip4jvm.io.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor
final class AesExtraDataRecordReader implements Reader<AesExtraDataRecord> {

    private final int signature;
    @NonNull
    private final Charset charset;

    @Override
    public AesExtraDataRecord read(@NonNull DataInput in) throws IOException {
        if (signature != AesExtraDataRecord.SIGNATURE)
            return AesExtraDataRecord.NULL;

        return AesExtraDataRecord.builder()
                                 .size(in.readWord())
                                 .versionNumber(in.readWord())
                                 .vendor(in.readString(2, charset))
                                 .strength(AesStrength.parseValue(in.readByte()))
                                 .compressionMethod(CompressionMethod.parseCode(in.readWord()))
                                 .build();
    }
}
