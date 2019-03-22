package net.lingala.zip4j.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.crypto.AESDecrypter;
import net.lingala.zip4j.crypto.Decrypter;
import net.lingala.zip4j.crypto.StandardDecrypter;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;

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
        public Decrypter createDecrypter(LittleEndianRandomAccessFile in, CentralDirectory.FileHeader fileHeader, LocalFileHeader localFileHeader)
                throws IOException {
            return null;
        }
    },
    STANDARD(0) {
        @Override
        public Decrypter createDecrypter(LittleEndianRandomAccessFile in, CentralDirectory.FileHeader fileHeader, LocalFileHeader localFileHeader)
                throws IOException {
            in.seek(localFileHeader.getOffsetStartOfData());
            return new StandardDecrypter(fileHeader, in.readBytes(InternalZipConstants.STD_DEC_HDR_SIZE));
        }
    },
    STRONG(1),
    AES(99) {
        @Override
        public Decrypter createDecrypter(LittleEndianRandomAccessFile in, CentralDirectory.FileHeader fileHeader, LocalFileHeader localFileHeader)
                throws IOException {
            byte[] salt = getSalt(in, localFileHeader);
            byte[] passwordVerifier = in.readBytes(2);
            return new AESDecrypter(localFileHeader, salt, passwordVerifier);
        }

        private byte[] getSalt(@NonNull LittleEndianRandomAccessFile in, @NonNull LocalFileHeader localFileHeader) throws IOException {
            if (localFileHeader.getAesExtraDataRecord() == null)
                return null;

            in.seek(localFileHeader.getOffsetStartOfData());
            return in.readBytes(localFileHeader.getAesExtraDataRecord().getAesStrength().getSaltLength());
        }
    };

    private final int value;

    public Decrypter createDecrypter(LittleEndianRandomAccessFile in, CentralDirectory.FileHeader fileHeader, LocalFileHeader localFileHeader)
            throws IOException {
        throw new ZipException("unsupported encryption method");
    }
}
