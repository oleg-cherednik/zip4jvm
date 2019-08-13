package com.cop.zip4j.model;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.crypto.aes.AesDecoder;
import com.cop.zip4j.crypto.aes.AesEncoder;
import com.cop.zip4j.crypto.pkware.PkwareDecoder;
import com.cop.zip4j.crypto.pkware.PkwareEncoder;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.aes.AesExtraDataRecord;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 09.03.2019
 */
@SuppressWarnings("MethodCanBeVariableArityMethod")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum Encryption {
    OFF(pathZipEntry -> Encoder.NULL) {
        @Override
        public Decoder decoder(@NonNull DataInput in, @NonNull LocalFileHeader localFileHeader, char[] password) throws IOException {
            return Decoder.NULL;
        }
    },
    PKWARE(PkwareEncoder::create) {
        @Override
        public Decoder decoder(@NonNull DataInput in, @NonNull LocalFileHeader localFileHeader, char[] password) throws IOException {
            return PkwareDecoder.create(in, localFileHeader, password);
        }
    },
    AES(AesEncoder::create) {
        @Override
        public Decoder decoder(DataInput in, @NonNull LocalFileHeader localFileHeader, char[] password) throws IOException {
            return AesDecoder.create(in, localFileHeader, password);
        }

        @Override
        public long getChecksum(long checksum) {
            return 0;
        }
    };

    private final Function<PathZipEntry, Encoder> createEncoder;

    public final Encoder encoder(@NonNull PathZipEntry entry) {
        return createEncoder.apply(entry);
    }

    public Decoder decoder(@NonNull DataInput in, @NonNull LocalFileHeader localFileHeader, char[] password) throws IOException {
        throw new Zip4jException("unsupported encryption method");
    }

    public long getChecksum(long checksum) {
        return checksum;
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


