package ru.olegcherednik.zip4jvm.crypto.aes;

import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.crypto.Cipher;

/**
 * @author Oleg Cherednik
 * @since 21.11.2024
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AesEcdDecoder implements Decoder {

    private final AesStrongEngine engine;
    @Getter
    private final long compressedSize;

    @SuppressWarnings("NewMethodNamingConvention")
    public static AesEcdDecoder create128(DecryptionHeader decryptionHeader,
                                          char[] password,
                                          long compressedSize,
                                          ByteOrder byteOrder) {
        return create(decryptionHeader, password, AesStrength.S128, compressedSize, byteOrder);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static AesEcdDecoder create192(DecryptionHeader decryptionHeader,
                                          char[] password,
                                          long compressedSize,
                                          ByteOrder byteOrder) {
        return create(decryptionHeader, password, AesStrength.S192, compressedSize, byteOrder);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static AesEcdDecoder create256(DecryptionHeader decryptionHeader,
                                          char[] password,
                                          long compressedSize,
                                          ByteOrder byteOrder) {
        return create(decryptionHeader, password, AesStrength.S256, compressedSize, byteOrder);
    }

    private static AesEcdDecoder create(DecryptionHeader decryptionHeader,
                                        char[] password,
                                        AesStrength strength,
                                        long compressedSize,
                                        ByteOrder byteOrder) {
        return Quietly.doQuietly(() -> {
            Cipher cipher = AesStrongEngine.createCipher(decryptionHeader, password, strength, byteOrder);
            AesStrongEngine engine = new AesStrongEngine(cipher);
            return new AesEcdDecoder(engine, compressedSize);
        });
    }

    // ---------- Decoder ----------

    @Override
    public int getBlockSize() {
        return engine.getBlockSize();
    }

    // ---------- Decrypt ----------

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        return engine.decrypt(buf, offs, len);
    }

}
