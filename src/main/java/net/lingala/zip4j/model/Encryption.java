package net.lingala.zip4j.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.crypto.AESDecoder;
import net.lingala.zip4j.crypto.AESEncoder;
import net.lingala.zip4j.crypto.Decoder;
import net.lingala.zip4j.crypto.Encoder;
import net.lingala.zip4j.crypto.StandardDecoder;
import net.lingala.zip4j.crypto.StandardEncoder;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.utils.InternalZipConstants;
import net.lingala.zip4j.io.LittleEndianRandomAccessFile;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 09.03.2019
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum Encryption {
    OFF(-1) {
        @Override
        public Decoder createDecrypter(LittleEndianRandomAccessFile in, CentralDirectory.FileHeader fileHeader, LocalFileHeader localFileHeader)
                throws IOException {
            return null;
        }

        @Override
        public Encoder createEncryptor(ZipParameters parameters, LocalFileHeader localFileHeader) {
            return Encoder.NULL;
        }
    },
    STANDARD(0) {
        @Override
        public Decoder createDecrypter(LittleEndianRandomAccessFile in, CentralDirectory.FileHeader fileHeader, LocalFileHeader localFileHeader)
                throws IOException {
            in.seek(localFileHeader.getOffs());
            return new StandardDecoder(fileHeader, in.readBytes(InternalZipConstants.STD_DEC_HDR_SIZE));
        }

        @Override
        public Encoder createEncryptor(ZipParameters parameters, LocalFileHeader localFileHeader) {
            // Since we do not know the crc here, we use the modification time for encrypting.
            return new StandardEncoder(parameters.getPassword(), (localFileHeader.getLastModifiedTime() & 0xFFFF) << 16);
        }
    },
    STRONG(1),
    AES(99) {
        @Override
        public Decoder createDecrypter(LittleEndianRandomAccessFile in, CentralDirectory.FileHeader fileHeader, LocalFileHeader localFileHeader)
                throws IOException {
            byte[] salt = getSalt(in, localFileHeader);
            byte[] passwordVerifier = in.readBytes(2);
            return new AESDecoder(localFileHeader, fileHeader.getPassword(), salt, passwordVerifier);
        }

        private byte[] getSalt(@NonNull LittleEndianRandomAccessFile in, @NonNull LocalFileHeader localFileHeader) throws IOException {
            if (localFileHeader.getExtraField().getAesExtraDataRecord() == AESExtraDataRecord.NULL)
                return null;

            in.seek(localFileHeader.getOffs());
            return in.readBytes(localFileHeader.getExtraField().getAesExtraDataRecord().getAesStrength().getSaltLength());
        }

        @Override
        public Encoder createEncryptor(ZipParameters parameters, LocalFileHeader localFileHeader) {
            return new AESEncoder(parameters.getPassword(), parameters.getAesKeyStrength());
        }
    };

    private final int value;

    public Decoder createDecrypter(LittleEndianRandomAccessFile in, CentralDirectory.FileHeader fileHeader, LocalFileHeader localFileHeader)
            throws IOException {
        throw new ZipException("unsupported encryption method");
    }

    public Encoder createEncryptor(ZipParameters parameters, LocalFileHeader localFileHeader) {
        throw new ZipException("invalid encryption method");
    }
}
