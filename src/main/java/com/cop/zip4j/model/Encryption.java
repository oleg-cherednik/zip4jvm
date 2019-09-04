package com.cop.zip4j.model;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.crypto.aes.AesDecoder;
import com.cop.zip4j.crypto.aes.AesEncoder;
import com.cop.zip4j.crypto.aes.AesEngine;
import com.cop.zip4j.crypto.aes.AesStrength;
import com.cop.zip4j.crypto.pkware.PkwareDecoder;
import com.cop.zip4j.crypto.pkware.PkwareEncoder;
import com.cop.zip4j.crypto.pkware.PkwareEngine;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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
            PathZipEntry::getChecksum,
            Compression::getMethod),
    PKWARE(PkwareEncoder::create, PkwareDecoder::create, PkwareEngine::getCompressedSize, PathZipEntry::getChecksum, Compression::getMethod),
    AES_128(AesEncoder::create, AesDecoder::create,
            uncompressedSize -> AesEngine.getCompressedSize(uncompressedSize, AesStrength.S128),
            entry -> 0L, compression -> CompressionMethod.AES),
    AES_192(AES_128.createEncoder, AES_128.createDecoder,
            uncompressedSize -> AesEngine.getCompressedSize(uncompressedSize, AesStrength.S192),
            AES_128.checksum, AES_128.compressionMethod),
    AES_256(AES_128.createEncoder, AES_128.createDecoder,
            uncompressedSize -> AesEngine.getCompressedSize(uncompressedSize, AesStrength.S256),
            AES_128.checksum, AES_128.compressionMethod);

    private final Function<PathZipEntry, Encoder> createEncoder;
    private final CreateDecoder createDecoder;
    private final LongFunction<Long> compressedSizeFunc;
    private final Function<PathZipEntry, Long> checksum;
    private final Function<Compression, CompressionMethod> compressionMethod;

    @NonNull
    public static Encryption get(@NonNull ExtraField extraField, @NonNull GeneralPurposeFlag generalPurposeFlag) {
        if (generalPurposeFlag.isStrongEncryption())
            throw new Zip4jException("Strong encryption is not supported");
        if (!generalPurposeFlag.isEncrypted())
            return OFF;
        if (extraField.getAesExtraDataRecord() != AesExtraDataRecord.NULL)
            return AesEngine.getEncryption(extraField.getAesExtraDataRecord().getStrength());
//        return generalPurposeFlag.isStrongEncryption() ? STRONG : PKWARE;
        return PKWARE;
    }

    public interface CreateDecoder {

        Decoder apply(PathZipEntry entry, DataInput in) throws IOException;
    }

}


