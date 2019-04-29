package net.lingala.zip4j.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.crypto.AesDecoder;
import net.lingala.zip4j.crypto.AesEncoder;
import net.lingala.zip4j.crypto.Decoder;
import net.lingala.zip4j.crypto.Encoder;
import net.lingala.zip4j.crypto.StandardDecoder;
import net.lingala.zip4j.crypto.StandardEncoder;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.LittleEndianRandomAccessFile;
import net.lingala.zip4j.utils.InternalZipConstants;

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
        public Encoder encoder(@NonNull LocalFileHeader localFileHeader, @NonNull ZipParameters parameters) {
            return Encoder.NULL;
        }
    },
    STANDARD(0) {
        @Override
        public Decoder decoder(@NonNull LittleEndianRandomAccessFile in, @NonNull LocalFileHeader localFileHeader, char[] password)
                throws IOException {
            in.seek(localFileHeader.getOffs());
            return new StandardDecoder(localFileHeader, password, in.readBytes(InternalZipConstants.STD_DEC_HDR_SIZE));
        }

        @Override
        public Encoder encoder(@NonNull LocalFileHeader localFileHeader, @NonNull ZipParameters parameters) {
            // Since we do not know the crc here, we use the modification time for encrypting.
            return new StandardEncoder(parameters.getPassword(), (localFileHeader.getLastModifiedTime() & 0xFFFF) << 16);
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
        public Encoder encoder(@NonNull LocalFileHeader localFileHeader, @NonNull ZipParameters parameters) {
            return new AesEncoder(parameters.getPassword(), parameters.getAesStrength());
        }
    };

    private final int value;

    public Decoder decoder(@NonNull LittleEndianRandomAccessFile in, @NonNull LocalFileHeader localFileHeader, char[] password) throws IOException {
        throw new ZipException("unsupported encryption method");
    }

    public Encoder encoder(@NonNull LocalFileHeader localFileHeader, @NonNull ZipParameters parameters) {
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


