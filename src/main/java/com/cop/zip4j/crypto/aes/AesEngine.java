package com.cop.zip4j.crypto.aes;

import com.cop.zip4j.model.aes.AesStrength;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

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

/**
 * @author Oleg Cherednik
 * @since 13.08.2019
 */
// TODO should be package
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AesEngine {

    public static final int AES_AUTH_LENGTH = 10;
    public static final int AES_BLOCK_SIZE = 16;
    public static final int AES_PASSWORD_VERIFIER_LENGTH = 2;

    protected final Cipher cipher;
    protected final Mac mac;

    private final byte[] iv = new byte[AES_BLOCK_SIZE];
    private final byte[] counter = new byte[iv.length];
    private int nonce = iv.length;

    /**
     * Custom implementation (com.sun.crypto.provider.CounterMode) of 'AES/CTR/NoPadding' is not compatible with WinZip specification.
     * Have to implement custom one.
     */
    protected final void cypherUpdate(byte[] buf, int offs, int len) throws ShortBufferException {
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

    public static byte[] createKey(char[] password, byte[] salt, AesStrength strength)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        final int iterationCount = 1000;
        final int keyLength = strength.getSize() * 2 + 16;
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
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

}
