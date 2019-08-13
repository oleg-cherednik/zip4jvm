package com.cop.zip4j.crypto.aes;

import com.cop.zip4j.model.aes.AesStrength;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Oleg Cherednik
 * @since 13.08.2019
 */
// TODO should be package
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AesEngine {

    public static final int AES_AUTH_LENGTH = 10;
    public static final int AES_BLOCK_SIZE = 16;
    public static final int PASSWORD_VERIFIER_LENGTH = 2;

    public static Cipher createCipher(byte[] key, AesStrength strength) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        final int offs = 0;
        final int len = strength.getKeyLength();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, offs, len, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        return cipher;
    }

    public static Mac createMac(byte[] key, AesStrength strength) throws NoSuchAlgorithmException, InvalidKeyException {
        final int offs = strength.getKeyLength();
        final int len = strength.getMacLength();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, offs, len, "HmacSHA1");

        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(secretKeySpec);
        return mac;
    }

    public static byte[] getPasswordChecksum(byte[] key, AesStrength strength) {
        byte[] buf = new byte[PASSWORD_VERIFIER_LENGTH];
        int offs = strength.getKeyLength() + strength.getMacLength();
        System.arraycopy(key, offs, buf, 0, buf.length);
        return buf;
    }

}
