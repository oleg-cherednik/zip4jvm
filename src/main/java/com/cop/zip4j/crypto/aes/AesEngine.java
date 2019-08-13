package com.cop.zip4j.crypto.aes;

import com.cop.zip4j.model.aes.AesStrength;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AesEngine {

    public static final int AES_AUTH_LENGTH = 10;
    public static final int AES_BLOCK_SIZE = 16;
    public static final int PASSWORD_VERIFIER_LENGTH = 2;

    public static byte[] createKey(char[] password, byte[] salt, AesStrength strength)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        final int iterationCount = 1000;
        final int keyLength = strength.getSize() * 2 + 16;
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(keySpec).getEncoded();
    }

    public static Cipher createCipher(SecretKeySpec secretKeySpec) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return cipher;
    }

    public static Mac createMac(SecretKeySpec secretKeySpec) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(secretKeySpec);
        return mac;
    }

}
