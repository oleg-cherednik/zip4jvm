package com.cop.zip4j.crypto.aesnew;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.crypto.aesnew.pbkdf2.MacBasedPRF;
import com.cop.zip4j.crypto.aesnew.pbkdf2.PBKDF2Engine;
import com.cop.zip4j.crypto.aesnew.pbkdf2.PBKDF2Parameters;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.AesExtraDataRecord;
import com.cop.zip4j.model.AesStrength;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.zip.ZipException;

import static com.cop.zip4j.crypto.aesnew.AesNewCipherUtil.prepareBuffAESIVBytes;
import static com.cop.zip4j.crypto.aesnew.AesNewEngine.AES_BLOCK_SIZE;

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

    public AesNewDecoder(AesExtraDataRecord aesExtraDataRecord, char[] password, byte[] salt, byte[] passwordVerifier) throws ZipException {
        this.aesExtraDataRecord = aesExtraDataRecord;
        this.password = password;
        this.saltLength = salt.length;
        try {
            iv = new byte[AES_BLOCK_SIZE];
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            KeySpec spec = new PBEKeySpec(password, salt, 1000, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            //AES/CBC/PKCS5Padding
            cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);


//            counterBlock = new byte[AES_BLOCK_SIZE];


//            init(salt, passwordVerifier);
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    private void init(byte[] salt, byte[] passwordVerifier) throws NoSuchAlgorithmException, InvalidKeySpecException {
        AesStrength aesKeyStrength = aesExtraDataRecord.getAesStrength();

        if (password == null || password.length <= 0) {
            throw new Zip4jException("empty or null password provided for AES Decryptor");
        }


        byte[] derivedKey = deriveKey(salt, password, aesKeyStrength.getKeyLength(), aesKeyStrength.getMacLength());
        if (derivedKey == null || derivedKey.length != (aesKeyStrength.getKeyLength() + aesKeyStrength.getMacLength()
                + PASSWORD_VERIFIER_LENGTH)) {
            throw new Zip4jException("invalid derived key");
        }

        byte[] aesKey = new byte[aesKeyStrength.getKeyLength()];
        macKey = new byte[aesKeyStrength.getMacLength()];
        byte[] derivedPasswordVerifier = new byte[PASSWORD_VERIFIER_LENGTH];

        System.arraycopy(derivedKey, 0, aesKey, 0, aesKeyStrength.getKeyLength());
        System.arraycopy(derivedKey, aesKeyStrength.getKeyLength(), macKey, 0, aesKeyStrength.getMacLength());
        System.arraycopy(derivedKey, aesKeyStrength.getKeyLength() + aesKeyStrength.getMacLength(), derivedPasswordVerifier,
                0, PASSWORD_VERIFIER_LENGTH);

        if (!Arrays.equals(passwordVerifier, derivedPasswordVerifier)) {
            throw new Zip4jException("Wrong Password");
        }

        aesEngine = new AesNewEngine(aesKey);
        mac = new MacBasedPRF("HmacSHA1");
        mac.init(macKey);
    }

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        try {
            int loop = len / AES_BLOCK_SIZE * AES_BLOCK_SIZE + (len % AES_BLOCK_SIZE == 0 ? 0 : AES_BLOCK_SIZE);
            byte[] res = cipher.doFinal(buf, offs, len);
            int a = 0;
            a++;

            for (int j = offs; j < (offs + len); j += AES_BLOCK_SIZE) {
                int loopCount = (j + AES_BLOCK_SIZE <= (offs + len)) ? AES_BLOCK_SIZE : ((offs + len) - j);

                mac.update(buf, j, loopCount);
                prepareBuffAESIVBytes(iv, nonce);
                aesEngine.processBlock(iv, counterBlock);

                for (int k = 0; k < loopCount; k++) {
                    buf[j + k] = (byte)(buf[j + k] ^ counterBlock[k]);
                }

                nonce++;
            }

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
