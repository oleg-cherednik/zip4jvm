package ru.olegcherednik.zip4jvm.crypto.aes.strong;

import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.exception.IncorrectZipEntryPasswordException;
import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.crypto.strong.DecryptionHeaderReader;
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
    private final AesStrength strength;

    public StrongAesDecoder createDecoder(long compressedSize, String fileName, DataInput in) {
        in.mark(DECRYPTION_HEADER);
        DecryptionHeader decryptionHeader = Quietly.doRuntime(() -> new DecryptionHeaderReader().read(in));
        StrongAesCipher cipher = createCipher(decryptionHeader, fileName);

        validatePasswordChecksum(cipher, decryptionHeader, fileName, in.getByteOrder());

        long dataCompressedSize = compressedSize - (int) in.getMarkSize(DECRYPTION_HEADER);
        return new StrongAesDecoder(cipher, dataCompressedSize);
    }

    private StrongAesCipher createCipher(DecryptionHeader decryptionHeader, String fileName) {
        try {
            return StrongAesCipher.getInstance(decryptionHeader, password, strength);
        } catch (IncorrectPasswordException e) {
            throw new IncorrectZipEntryPasswordException(fileName);
        }
    }

    private static void validatePasswordChecksum(StrongAesCipher cipher,
                                                 DecryptionHeader decryptionHeader,
                                                 String fileName,
                                                 ByteOrder byteOrder) {
        byte[] passwordValidationData = cipher.update(decryptionHeader.getPasswordValidationData());
        long actual = DecryptionHeader.getActualCrc32(passwordValidationData);
        long expected = DecryptionHeader.getExpectedCrc32(passwordValidationData, byteOrder);

        if (expected != actual)
            throw new IncorrectZipEntryPasswordException(fileName);
    }

}
