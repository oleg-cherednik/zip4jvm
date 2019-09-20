package ru.olegcherednik.zip4jvm.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.crypto.aes.AesDecoder;
import ru.olegcherednik.zip4jvm.crypto.aes.AesEncoder;
import ru.olegcherednik.zip4jvm.crypto.aes.AesEngine;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.pkware.PkwareDecoder;
import ru.olegcherednik.zip4jvm.crypto.pkware.PkwareEncoder;
import ru.olegcherednik.zip4jvm.crypto.pkware.PkwareEngine;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * @author Oleg Cherednik
 * @since 09.03.2019
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum Encryption {
    OFF(entry -> Encoder.NULL,
            (entry, in) -> Decoder.NULL,
            uncompressedSize -> uncompressedSize,
            ZipEntry::getChecksum,
            Compression::getMethod),
    PKWARE(PkwareEncoder::create, PkwareDecoder::create, PkwareEngine::getCompressedSize, ZipEntry::getChecksum, Compression::getMethod),
    AES_128(AesEncoder::create, AesDecoder::create,
            uncompressedSize -> AesEngine.getCompressedSize(uncompressedSize, AesStrength.S128),
            entry -> 0L, compression -> CompressionMethod.AES),
    AES_192(AES_128.createEncoder, AES_128.createDecoder,
            uncompressedSize -> AesEngine.getCompressedSize(uncompressedSize, AesStrength.S192),
            AES_128.checksum, AES_128.compressionMethod),
    AES_256(AES_128.createEncoder, AES_128.createDecoder,
            uncompressedSize -> AesEngine.getCompressedSize(uncompressedSize, AesStrength.S256),
            AES_128.checksum, AES_128.compressionMethod);

    private final Function<ZipEntry, Encoder> createEncoder;
    private final CreateDecoder createDecoder;
    private final LongFunction<Long> expectedCompressedSizeFunc;
    private final Function<ZipEntry, Long> checksum;
    private final Function<Compression, CompressionMethod> compressionMethod;

    @NonNull
    public static Encryption get(@NonNull ExtraField extraField, @NonNull GeneralPurposeFlag generalPurposeFlag) {
        if (generalPurposeFlag.isStrongEncryption())
            throw new Zip4jvmException("Strong encryption is not supported");
        if (!generalPurposeFlag.isEncrypted())
            return OFF;
        if (extraField.getAesExtraDataRecord() != AesExtraDataRecord.NULL)
            return AesEngine.getEncryption(extraField.getAesExtraDataRecord().getStrength());
//        return generalPurposeFlag.isStrongEncryption() ? STRONG : PKWARE;
        return PKWARE;
    }

    public interface CreateDecoder {

        Decoder apply(ZipEntry entry, DataInput in) throws IOException;
    }

}


