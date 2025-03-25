package ru.olegcherednik.zip4jvm.crypto.aes.strong.cd;

import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.aes.strong.StrongAesCipher;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.exception.IncorrectCentralDirectoryPasswordException;
import ru.olegcherednik.zip4jvm.io.ByteOrder;

import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 25.03.2025
 */
@RequiredArgsConstructor
public final class StrongCentralDirectoryAesFactory {

    private final char[] password;

    public AesCentralDirectoryDecoder createDecoder(long compressedSize,
                                                    DecryptionHeader decryptionHeader,
                                                    AesStrength strength,
                                                    ByteOrder byteOrder) {
        StrongAesCipher cipher = createCipher(decryptionHeader, strength, byteOrder);
        byte[] passwordValidationData = cipher.update(decryptionHeader.getPasswordValidationData());
        validatePasswordChecksum(passwordValidationData, byteOrder);
        return new AesCentralDirectoryDecoder(cipher, compressedSize);
    }

    private StrongAesCipher createCipher(DecryptionHeader decryptionHeader, AesStrength strength, ByteOrder byteOrder) {
        return StrongAesCipher.getInstance(decryptionHeader, password, strength, byteOrder);
    }

    private static void validatePasswordChecksum(byte[] passwordValidationData, ByteOrder byteOrder) {
        long actual = DecryptionHeader.getActualCrc32(passwordValidationData);
        long expected = DecryptionHeader.getExpectedCrc32(passwordValidationData, byteOrder);

        if (expected != actual)
            throw new IncorrectCentralDirectoryPasswordException();
    }

}
