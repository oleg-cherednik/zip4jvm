package ru.olegcherednik.zip4jvm.crypto.strong.cd;

import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.strong.BaseStrongCipher;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.aes.StrongAesCipher;
import ru.olegcherednik.zip4jvm.crypto.strong.tripledes.StrongTripleDesCipher;
import ru.olegcherednik.zip4jvm.crypto.strong.tripledes.TripleDesStrength;
import ru.olegcherednik.zip4jvm.exception.EncryptionNotSupportedException;
import ru.olegcherednik.zip4jvm.exception.IncorrectCentralDirectoryPasswordException;
import ru.olegcherednik.zip4jvm.io.ByteOrder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.Supplier;

/**
 * @author Oleg Cherednik
 * @since 22.07.2026
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class StrongCipherFactory {

    public static StrongAesCipher createStrongAesCipher(char[] password,
                                                        DecryptionHeader decryptionHeader,
                                                        ByteOrder byteOrder) {
        return createStrongCipher(() -> createAesCipherInstance(password, decryptionHeader),
                                  decryptionHeader, byteOrder);
    }

    public static StrongTripleDesCipher createStrongTripleDesCipher(char[] password,
                                                                    DecryptionHeader decryptionHeader,
                                                                    ByteOrder byteOrder) {
        return createStrongCipher(() -> createTripleDesCipherInstance(password, decryptionHeader),
                                  decryptionHeader, byteOrder);
    }

    private static StrongAesCipher createAesCipherInstance(char[] password, DecryptionHeader decryptionHeader) {
        EncryptionAlgorithm encryptionAlgorithm = decryptionHeader.getEncryptionAlgorithm();
        AesStrength strength = AesStrength.of(encryptionAlgorithm.getEncryption());

        if (strength == AesStrength.NULL)
            throw new EncryptionNotSupportedException(encryptionAlgorithm);

        return StrongAesCipher.getInstance(decryptionHeader, password, strength);
    }

    private static StrongTripleDesCipher createTripleDesCipherInstance(char[] password,
                                                                       DecryptionHeader decryptionHeader) {
        EncryptionAlgorithm encryptionAlgorithm = decryptionHeader.getEncryptionAlgorithm();
        TripleDesStrength strength = TripleDesStrength.of(encryptionAlgorithm.getEncryption());

        if (strength == TripleDesStrength.NULL)
            throw new EncryptionNotSupportedException(encryptionAlgorithm);

        return StrongTripleDesCipher.getInstance(decryptionHeader, password, strength);
    }

    private static <T extends BaseStrongCipher> T createStrongCipher(Supplier<T> strongCipherSup,
                                                                     DecryptionHeader decryptionHeader,
                                                                     ByteOrder byteOrder) {
        T cipher = strongCipherSup.get();
        byte[] passwordValidationData = cipher.update(decryptionHeader.getPasswordValidationData());
        validatePasswordChecksum(passwordValidationData, byteOrder);
        return cipher;
    }

    private static void validatePasswordChecksum(byte[] passwordValidationData, ByteOrder byteOrder) {
        long actual = DecryptionHeader.getActualCrc32(passwordValidationData);
        long expected = DecryptionHeader.getExpectedCrc32(passwordValidationData, byteOrder);

        if (expected != actual)
            throw new IncorrectCentralDirectoryPasswordException();
    }

}
