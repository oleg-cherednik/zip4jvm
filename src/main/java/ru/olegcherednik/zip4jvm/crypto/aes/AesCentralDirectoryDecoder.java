package ru.olegcherednik.zip4jvm.crypto.aes;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.CentralDirectoryDecoder;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.io.Endianness;

import javax.crypto.Cipher;

/**
 * @author Oleg Cherednik
 * @since 31.08.2023
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AesCentralDirectoryDecoder implements CentralDirectoryDecoder {

    private final Cipher cipher;

    public static AesCentralDirectoryDecoder create(char[] password, Endianness endianness, DecryptionHeader decryptionHeader) {
        Cipher cipher = AesDecryptionHeaderDecoder.createCipher(password, endianness, decryptionHeader);
        return new AesCentralDirectoryDecoder(cipher);
    }

    // ---------- CentralDirectoryCipher ----------

    @Override
    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public byte[] decrypt(byte[] buf) {
        return cipher.update(buf);
    }

}
