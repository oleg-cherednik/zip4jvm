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
            entry -> 0L,
            crc32 -> crc32),
    PKWARE(PkwareEncoder::create,
            PkwareDecoder::create,
            entry -> entry.size() + PkwareHeader.SIZE,
            crc32 -> crc32),
    AES(AesEncoder::create,
            AesDecoder::create,
            entry -> entry.size() + entry.getStrength().saltLength() + AesEngine.MAX_SIZE + AesEngine.PASSWORD_CHECKSUM_SIZE,
            crc32 -> 0L) {
        @Override
        public CompressionMethod getCompressionMethod(PathZipEntry entry) {
            return CompressionMethod.AES_ENC;
        }
    };

    private final Function<PathZipEntry, Encoder> createEncoder;
    private final CreateDecoder createDecoder;
    private final Function<PathZipEntry, Long> compressedSize;
    private final LongFunction<Long> checksumFileHeader;

    public long getChecksumFileHeader(CentralDirectory.FileHeader fileHeader) {
        return fileHeader.getCrc32();
    }

    @NonNull
    public CompressionMethod getCompressionMethod(PathZipEntry entry) {
        return entry.getCompression().getMethod();
    }

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


