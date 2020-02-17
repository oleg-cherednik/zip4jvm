package ru.olegcherednik.zip4jvm.crypto.aes;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * @author Oleg Cherednik
 * @since 13.08.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class AesEngine {

    public static final int MAC_SIZE = 10;
    public static final int PASSWORD_CHECKSUM_SIZE = 2;
    private static final int BLOCK_SIZE = 16;
    private static final int ITERATION_COUNT = 1000;

    private final Cipher cipher;
    private final Mac mac;
    private final byte[] iv = new byte[BLOCK_SIZE];
    private final byte[] counter = new byte[BLOCK_SIZE];

    private int nonce = BLOCK_SIZE;

    /*
     * Sun implementation (com.sun.crypto.provider.CounterMode) of 'AES/CTR/NoPadding' is not compatible with WinZip specification.
     * Have to implement custom one.
     */
    public void cypherUpdate(byte[] buf, int offs, int len) throws ShortBufferException {
        for (int i = 0; i < len; i++) {
            if (nonce == iv.length) {
                ivUpdate();
                cipher.update(iv, 0, iv.length, counter);
                nonce = 0;
            }

            buf[offs + i] ^= counter[nonce++];
        }
    }

    private void ivUpdate() {
        for (int i = 0; i < iv.length; i++)
            if (++iv[i] != 0)
                break;
    }

    public void updateMac(byte[] buf, int offs, int len) {
        mac.update(buf, offs, len);
    }

    public byte[] getMac() {
        return mac.doFinal();
    }

    public static byte[] createKey(char[] password, byte[] salt, AesStrength strength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        int keyLength = strength.getSize() * 2 + 16;
        KeySpec keySpec = new PBEKeySpec(password, salt, ITERATION_COUNT, keyLength);
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(keySpec).getEncoded();
    }

    public static Cipher createCipher(SecretKeySpec secretKeySpec) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("AES");
        // use custom AES implementation, so no worry for DECRYPT_MODE
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return cipher;
    }

    public static Mac createMac(SecretKeySpec secretKeySpec) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(secretKeySpec);
        return mac;
    }

    public static AesStrength getStrength(EncryptionMethod encryptionMethod) {
        if (encryptionMethod == EncryptionMethod.AES_128)
            return AesStrength.S128;
        if (encryptionMethod == EncryptionMethod.AES_192)
            return AesStrength.S192;
        if (encryptionMethod == EncryptionMethod.AES_256)
            return AesStrength.S256;
        return AesStrength.NULL;
    }

    public static EncryptionMethod getEncryption(AesStrength strength) {
        if (strength == AesStrength.S128)
            return EncryptionMethod.AES_128;
        if (strength == AesStrength.S192)
            return EncryptionMethod.AES_192;
        if (strength == AesStrength.S256)
            return EncryptionMethod.AES_256;
        return EncryptionMethod.OFF;
    }

    public static long getDataCompressedSize(long compressedSize, int saltLength) {
        return compressedSize - saltLength - PASSWORD_CHECKSUM_SIZE - MAC_SIZE;
    }

}
