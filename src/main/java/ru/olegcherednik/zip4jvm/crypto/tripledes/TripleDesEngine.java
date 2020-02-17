package ru.olegcherednik.zip4jvm.crypto.tripledes;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;

import javax.crypto.Cipher;

/**
 * @author Oleg Cherednik
 * @since 16.02.2020
 */
@RequiredArgsConstructor
public final class TripleDesEngine {

    private final Cipher cipher;

    public void cypherUpdate(byte[] buf, int offs, int len) {
        cipher.update(buf, offs, len);
    }

    public static TripleDesStrength getStrength(EncryptionMethod encryptionMethod) {
        if (encryptionMethod == EncryptionMethod.TRIPLE_DES_168)
            return TripleDesStrength.S168;
        if (encryptionMethod == EncryptionMethod.TRIPLE_DES_192)
            return TripleDesStrength.S192;
        return TripleDesStrength.NULL;
    }
}
