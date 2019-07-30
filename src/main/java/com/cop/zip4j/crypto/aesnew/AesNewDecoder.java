package com.cop.zip4j.crypto.aesnew;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.crypto.aesnew.pbkdf2.MacBasedPRF;
import com.cop.zip4j.crypto.aesnew.pbkdf2.PBKDF2Engine;
import com.cop.zip4j.crypto.aesnew.pbkdf2.PBKDF2Parameters;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.AesExtraDataRecord;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.zip.ZipException;

public class AesNewDecoder implements Decoder {

    public static final int PASSWORD_VERIFIER_LENGTH = 2;

    private AesExtraDataRecord aesExtraDataRecord;
    private char[] password;
    private AesNewEngine aesEngine;
    private MacBasedPRF mac;

    private int nonce = 1;
    private byte[] iv;
    private byte[] counterBlock;

    private byte[] macKey;
    private int saltLength;

    public byte[] getStoredMac() {
        return macKey;
    }

    public void setStoredMac(byte[] storedMac) {
        this.macKey = storedMac;
    }

    public int getSaltLength() {
        return saltLength;
    }

    public int getPasswordVerifierLength() {
        return PASSWORD_VERIFIER_LENGTH;
    }

    private Cipher cipher;

    private static final String aes = "AES/CTR/PKCS5Padding";
    private static final String pbk = "PBKDF2WithHmacSHA1";

    public AesNewDecoder(AesExtraDataRecord aesExtraDataRecord, char[] password, byte[] salt, byte[] passwordVerifier) throws ZipException {
        this.aesExtraDataRecord = aesExtraDataRecord;
        this.password = password;
        this.saltLength = salt.length;

        try {
            byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance(pbk);
            KeySpec spec = new PBEKeySpec(password, salt, 1000, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            cipher = Cipher.getInstance(aes);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);

            //AES/CBC/PKCS5Padding
            //AES/CTR/NoPadding
//            cipher = Cipher.getInstance("AES/CTR/NoPadding");
//            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);


//            counterBlock = new byte[AES_BLOCK_SIZE];


//            init(salt, passwordVerifier);
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        try {
            byte[] tmp = Arrays.copyOfRange(buf, 0, len);
            byte[] res = cipher.doFinal(tmp);
            int a = 0;
            a++;

        } catch(Exception e) {
            throw new Zip4jException(e);
        }

        return len;
    }

    private byte[] deriveKey(byte[] salt, char[] password, int keyLength, int macLength) {
        PBKDF2Parameters p = new PBKDF2Parameters("HmacSHA1", "ISO-8859-1", salt, 1000);
        PBKDF2Engine e = new PBKDF2Engine(p);
        return e.deriveKey(password, keyLength + macLength + PASSWORD_VERIFIER_LENGTH);
    }

    public byte[] getCalculatedAuthenticationBytes() {
        return mac.doFinal();
    }


}
