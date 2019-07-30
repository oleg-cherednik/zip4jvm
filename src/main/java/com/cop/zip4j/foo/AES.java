package com.cop.zip4j.foo;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;

/**
 * @author Oleg Cherednik
 * @since 30.07.2019
 */
public class AES {

    private static final byte[] salt = {
            (byte)0xEB, (byte)0x70, (byte)0x15, (byte)0xAE, (byte)0xC6,
            (byte)0xE7, (byte)0x4A, (byte)0x2C, (byte)0x92, (byte)0xBE,
            (byte)0x5B, (byte)0x9F, (byte)0x7C, (byte)0xC5, (byte)0xE6, (byte)0x70 };

    //    private static final String aes = "AES/CTR/NoPadding";
    private static final String aes = "AES/CTR/NoPadding";
    private static final String pbk = "PBKDF2WithHmacSHA1";

    public static byte[] encrypt(String str, String secret) {
        try {
            byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance(pbk);
            KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt, 1000, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance(aes);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));
        } catch(Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static String decrypt(byte[] buf, String secret) {
        try {
            byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance(pbk);
            KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt, 1000, 256);
            SecretKey tmp = factory.generateSecret(spec);
            byte[] keyb = tmp.getEncoded();
            SecretKeySpec skey = new SecretKeySpec(keyb, "AES");

            Cipher cipher = Cipher.getInstance(aes);
            cipher.init(Cipher.DECRYPT_MODE, skey, ivspec);
            return new String(cipher.doFinal(buf), StandardCharsets.UTF_8);
        } catch(Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }
}
