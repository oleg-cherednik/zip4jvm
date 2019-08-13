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
import lombok.NonNull;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 09.03.2019
 */
@SuppressWarnings("MethodCanBeVariableArityMethod")
public enum Encryption {
    OFF {
        @Override
        public Encoder encoder(@NonNull PathZipEntry entry) {
            return Encoder.NULL;
        }

        @Override
        public Decoder decoder(@NonNull DataInput in, @NonNull LocalFileHeader localFileHeader, char[] password) throws IOException {
            return Decoder.NULL;
        }
    },
    PKWARE {
        @Override
        public Encoder encoder(@NonNull PathZipEntry entry) {
            return PkwareEncoder.create(entry);
        }

        @Override
        public Decoder decoder(@NonNull DataInput in, @NonNull LocalFileHeader localFileHeader, char[] password) throws IOException {
            return PkwareDecoder.create(in, localFileHeader, password);
        }
    },
    STRONG,
    AES {
        @Override
        public Encoder encoder(@NonNull PathZipEntry entry) {
            return AesEncoder.create(entry.getStrength(), entry.getPassword());
        }

        @Override
        public Decoder decoder(@NonNull DataInput in, @NonNull LocalFileHeader localFileHeader, char[] password)
                throws IOException {
            return AesDecoder.create(in, localFileHeader, password);
        }

        @Override
        public long getChecksum(long checksum) {
            return 0;
        }
    };

    public Encoder encoder(@NonNull PathZipEntry entry) {
        throw new Zip4jException("invalid encryption method");
    }

    public Decoder decoder(@NonNull DataInput in, @NonNull LocalFileHeader localFileHeader, char[] password) throws IOException {
        throw new Zip4jException("unsupported encryption method");
    }

    public long getChecksum(long checksum) {
        return checksum;
    }

    public static Encryption get(@NonNull ExtraField extraField, @NonNull GeneralPurposeFlag generalPurposeFlag) {
        if (!generalPurposeFlag.isEncrypted())
            return OFF;
        if (extraField.getAesExtraDataRecord() != AesExtraDataRecord.NULL)
            return AES;
        return generalPurposeFlag.isStrongEncryption() ? STRONG : PKWARE;
    }

}


