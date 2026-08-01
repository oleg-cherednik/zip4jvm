package ru.olegcherednik.zip4jvm.crypto.strong;

import ru.olegcherednik.zip4jvm.crypto.DecoderFactory;
import ru.olegcherednik.zip4jvm.crypto.strong.cipher.StrongCipher;
import ru.olegcherednik.zip4jvm.crypto.strong.cipher.StrongCipherFactory;
import ru.olegcherednik.zip4jvm.io.ByteOrder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 01.08.2026
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class DecoderFactoryWithStrongCipher implements DecoderFactory {

    protected final StrongCipherFactory cipherFactory;

    public StrongCipher createStrongCipher(char[] password, DecryptionHeader decryptionHeader, ByteOrder byteOrder) {
        return cipherFactory.createStrongCipher(password, decryptionHeader, byteOrder);
    }

}
