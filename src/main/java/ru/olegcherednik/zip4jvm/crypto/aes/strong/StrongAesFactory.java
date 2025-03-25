package ru.olegcherednik.zip4jvm.crypto.aes.strong;

import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.exception.IncorrectZipEntryPasswordException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.crypto.strong.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
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

    //    public AesStrongDecoder createDecoder(long compressedSize, String fileName, DataInput in) {
    public AesStrongDecoder createDecoder(ZipEntry zipEntry, DataInput in) {
        return Quietly.doRuntime(() -> {
            in.mark(DECRYPTION_HEADER);
            DecryptionHeader decryptionHeader = new DecryptionHeaderReader().read(in);
            StrongAesCipher cipher = createCipher(decryptionHeader, zipEntry, in);
            int decryptionHeaderSize = (int) in.getMarkSize(DECRYPTION_HEADER);
            long compressedSize = zipEntry.getCompressedSize() - decryptionHeaderSize;
            return new AesStrongDecoder(decryptionHeader.getEncryptionAlgorithm(), cipher, compressedSize);
        });

        /*
        byte[] salt = Quietly.doRuntime(() -> in.readBytes(strength.getSaltSize()));
        byte[] key = createKey(salt);

        validatePasswordChecksum(key, fileName, in);

        WinZipAesCipher cipher = createCipher(key);
        Mac mac = createMac(key);
        long dataCompressedSize = getDataCompressedSize(compressedSize);

        return new AesDecoder(cipher, mac, dataCompressedSize);
         */
    }

    //    private StrongAesCipher createCipher(byte[] key) {
    private static StrongAesCipher createCipher(DecryptionHeader decryptionHeader, ZipEntry zipEntry, DataInput in) {
        try {
            EncryptionAlgorithm encryptionAlgorithm = decryptionHeader.getEncryptionAlgorithm();
            EncryptionMethod encryptionMethod = encryptionAlgorithm.getEncryptionMethod();
            AesStrength strength = AesStrength.of(encryptionMethod);
            return StrongAesCipher.getInstance(decryptionHeader,
                                               zipEntry.getPassword(),
                                               strength,
                                               in.getByteOrder());
        } catch (IncorrectPasswordException e) {
            throw new IncorrectZipEntryPasswordException(zipEntry.getFileName());
        }
    }

}
