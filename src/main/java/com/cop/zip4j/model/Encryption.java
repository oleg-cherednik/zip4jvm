package com.cop.zip4j.model;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.crypto.aes.AesDecoder;
import com.cop.zip4j.crypto.aes.AesEncoder;
import com.cop.zip4j.crypto.pkware.StandardDecoder;
import com.cop.zip4j.crypto.pkware.StandardEncoder;
import com.cop.zip4j.exception.ZipException;
import com.cop.zip4j.io.LittleEndianRandomAccessFile;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 09.03.2019
 */
@Getter
@SuppressWarnings("MethodCanBeVariableArityMethod")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum Encryption {
    OFF(-1) {
        @Override
        public Decoder decoder(@NonNull LittleEndianRandomAccessFile in, @NonNull LocalFileHeader localFileHeader, char[] password)
                throws IOException {
            return null;
        }

        @Override
        public Encoder encoder(@NonNull PathZipEntry entry) {
            return Encoder.NULL;
        }
    },
    STANDARD(0) {
        @Override
        public Decoder decoder(@NonNull LittleEndianRandomAccessFile in, @NonNull LocalFileHeader localFileHeader, char[] password)
                throws IOException {
            in.seek(localFileHeader.getOffs());
            return new StandardDecoder(localFileHeader, password, in.readBytes(StandardEncoder.SIZE_HEADER));
        }

        @Override
        public Encoder encoder(@NonNull PathZipEntry entry) {
            if(ArrayUtils.isEmpty(entry.getPassword()))
                throw new ZipException("Passwords should not be empty for '" + name() + "' encryption");
            return new StandardEncoder(entry.getPassword());
        }
    },
    STRONG(1),
    AES(99) {
        @Override
        public Decoder decoder(@NonNull LittleEndianRandomAccessFile in, @NonNull LocalFileHeader localFileHeader, char[] password)
                throws IOException {
            byte[] salt = getSalt(in, localFileHeader);
            byte[] passwordVerifier = in.readBytes(2);
            return new AesDecoder(localFileHeader, password, salt, passwordVerifier);
        }

        private byte[] getSalt(@NonNull LittleEndianRandomAccessFile in, @NonNull LocalFileHeader localFileHeader) throws IOException {
            if (localFileHeader.getEncryption() != AES)
                return null;

            in.seek(localFileHeader.getOffs());
            AesStrength aesStrength = localFileHeader.getExtraField().getAesExtraDataRecord().getAesStrength();
            return in.readBytes(aesStrength.getSaltLength());
        }

        @Override
        public Encoder encoder(@NonNull PathZipEntry entry) {
            return new AesEncoder(entry.getPassword(), entry.getAesStrength());
        }
    };

    private final int value;

    public Decoder decoder(@NonNull LittleEndianRandomAccessFile in, @NonNull LocalFileHeader localFileHeader, char[] password) throws IOException {
        throw new ZipException("unsupported encryption method");
    }

    public Encoder encoder(@NonNull PathZipEntry entry) {
        throw new ZipException("invalid encryption method");
    }

    public static Encryption get(@NonNull ExtraField extraField, @NonNull GeneralPurposeFlag generalPurposeFlag) {
        if (extraField.getAesExtraDataRecord() != AesExtraDataRecord.NULL)
            return AES;
        if (generalPurposeFlag.isStrongEncryption())
            return STRONG;
        return generalPurposeFlag.isEncrypted() ? STANDARD : OFF;
    }

}


