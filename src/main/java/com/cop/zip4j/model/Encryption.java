package com.cop.zip4j.model;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.crypto.aes.AesDecoder;
import com.cop.zip4j.crypto.aes.AesEncoder;
import com.cop.zip4j.crypto.aes.AesEngine;
import com.cop.zip4j.crypto.pkware.PkwareDecoder;
import com.cop.zip4j.crypto.pkware.PkwareEncoder;
import com.cop.zip4j.crypto.pkware.PkwareHeader;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.aes.AesExtraDataRecord;
import com.cop.zip4j.model.entry.PathZipEntry;
import com.cop.zip4j.model.entry.ZipEntry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 09.03.2019
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum Encryption {
    OFF(entry -> Encoder.NULL,
            (entry, in) -> Decoder.NULL,
            entry -> 0L,
            ZipEntry::checksum,
            entry -> entry.getCompression().getMethod()),
    PKWARE(PkwareEncoder::create,
            PkwareDecoder::create,
            entry -> entry.size() + PkwareHeader.SIZE,
            ZipEntry::checksum,
            entry -> entry.getCompression().getMethod()),
    AES(AesEncoder::create,
            AesDecoder::create,
            entry -> entry.size() + entry.getStrength().saltLength() + AesEngine.MAX_SIZE + AesEngine.PASSWORD_CHECKSUM_SIZE,
            entry -> 0L,
            entry -> CompressionMethod.AES_ENC);

    private final Function<PathZipEntry, Encoder> createEncoder;
    private final CreateDecoder createDecoder;
    private final Function<PathZipEntry, Long> compressedSize;
    private final Function<PathZipEntry, Long> checksum;
    private final Function<PathZipEntry, CompressionMethod> compressionMethod;

    @NonNull
    public static Encryption get(@NonNull ExtraField extraField, @NonNull GeneralPurposeFlag generalPurposeFlag) {
        if (generalPurposeFlag.isStrongEncryption())
            throw new Zip4jException("Strong encryption is not supported");
        if (!generalPurposeFlag.isEncrypted())
            return OFF;
        if (extraField.getAesExtraDataRecord() != AesExtraDataRecord.NULL)
            return AES;
//        return generalPurposeFlag.isStrongEncryption() ? STRONG : PKWARE;
        return PKWARE;
    }

    public interface CreateDecoder {

        Decoder apply(PathZipEntry entry, DataInput in) throws IOException;
    }

}


