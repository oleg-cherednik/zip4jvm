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

/**
 * @author Oleg Cherednik
 * @since 09.03.2019
 */
@Getter
@SuppressWarnings("MethodCanBeVariableArityMethod")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum Encryption {
    OFF(pathZipEntry -> Encoder.NULL,
            entry -> 0L,
            CentralDirectory.FileHeader::getCrc32) {
        @Override
        public Decoder decoder(@NonNull DataInput in, @NonNull LocalFileHeader localFileHeader, char[] password) throws IOException {
            return Decoder.NULL;
        }
    },
    PKWARE(PkwareEncoder::create,
            entry -> entry.size() + PkwareHeader.SIZE,
            CentralDirectory.FileHeader::getCrc32) {
        @Override
        public Decoder decoder(@NonNull DataInput in, @NonNull LocalFileHeader localFileHeader, char[] password) throws IOException {
            return PkwareDecoder.create(in, localFileHeader, password);
        }
    },
    AES(AesEncoder::create,
            entry -> entry.size() + entry.getStrength().saltLength() + AesEngine.MAX_SIZE + AesEngine.PASSWORD_CHECKSUM_SIZE,
            fileHeader -> 0L) {
        @Override
        public Decoder decoder(DataInput in, @NonNull LocalFileHeader localFileHeader, char[] password) throws IOException {
            return AesDecoder.create(in, localFileHeader, password);
        }
    };

    private final Function<PathZipEntry, Encoder> createEncoder;
    private final Function<PathZipEntry, Long> compressedSize;
    private final Function<CentralDirectory.FileHeader, Long> checksum;

    public Decoder decoder(@NonNull DataInput in, @NonNull LocalFileHeader localFileHeader, char[] password) throws IOException {
        throw new Zip4jException("unsupported encryption method");
    }

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

}


