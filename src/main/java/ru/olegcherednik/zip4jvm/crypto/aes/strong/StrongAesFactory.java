package ru.olegcherednik.zip4jvm.crypto.aes.strong;

import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.exception.IncorrectZipEntryPasswordException;
import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.crypto.strong.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 25.03.2025
 */
@RequiredArgsConstructor
public final class StrongAesFactory {

    private static final String DECRYPTION_HEADER = "AesStrongDecoder.DecryptionHeader";

    private final char[] password;

    public AesStrongDecoder createDecoder(long compressedSize, String fileName, DataInput in) {
        in.mark(DECRYPTION_HEADER);
        DecryptionHeader decryptionHeader = Quietly.doRuntime(() -> new DecryptionHeaderReader().read(in));
        StrongAesCipher cipher = createCipher(decryptionHeader, in.getByteOrder());
        byte[] passwordValidationData = cipher.update(decryptionHeader.getPasswordValidationData());

        validatePasswordChecksum(passwordValidationData, fileName, in.getByteOrder());

        long dataCompressedSize = compressedSize - (int) in.getMarkSize(DECRYPTION_HEADER);
        return new AesStrongDecoder(cipher, dataCompressedSize);
    }

    public AesCentralDirectoryDecoder createCentralDirectoryDecoder(long compressedSize,
                                                                    DecryptionHeader decryptionHeader,
                                                                    AesStrength strength,
                                                                    ByteOrder byteOrder) {
        StrongAesCipher cipher = AesStrongEngine.createCipher(decryptionHeader, password, strength, byteOrder);
        return new AesCentralDirectoryDecoder(cipher, compressedSize);
    }

    private StrongAesCipher createCipher(DecryptionHeader decryptionHeader, ByteOrder byteOrder) {
        EncryptionAlgorithm encryptionAlgorithm = decryptionHeader.getEncryptionAlgorithm();
        EncryptionMethod encryptionMethod = encryptionAlgorithm.getEncryptionMethod();
        AesStrength strength = AesStrength.of(encryptionMethod);
        return StrongAesCipher.getInstance(decryptionHeader, password, strength, byteOrder);
    }

    private static void validatePasswordChecksum(byte[] passwordValidationData, String fileName, ByteOrder byteOrder) {
        long actual = DecryptionHeader.getActualCrc32(passwordValidationData);
        long expected = DecryptionHeader.getExpectedCrc32(passwordValidationData, byteOrder);

        if (expected != actual)
            throw new IncorrectZipEntryPasswordException(fileName);
    }

}
