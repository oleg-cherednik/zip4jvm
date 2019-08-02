package com.cop.zip4j.crypto.aes;

import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.crypto.aes.pbkdf2.MacBasedPRF;
import com.cop.zip4j.crypto.aes.pbkdf2.PBKDF2Engine;
import com.cop.zip4j.crypto.aes.pbkdf2.PBKDF2Parameters;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.DataOutputStream;
import com.cop.zip4j.io.SplitOutputStream;
import com.cop.zip4j.model.aes.AesStrength;
import lombok.NonNull;

import java.io.IOException;

import static com.cop.zip4j.crypto.aes.AesCipherUtil.prepareBuffAESIVBytes;
import static com.cop.zip4j.crypto.aes.AesEngine.AES_BLOCK_SIZE;

public class AesEncoder implements Encoder {

    private static final int PASSWORD_VERIFIER_LENGTH = 2;

    private char[] password;
    private AesStrength aesKeyStrength;
    private AesEngine aesEngine;
    private MacBasedPRF mac;

    private boolean finished;

    private int nonce = 1;
    private int loopCount = 0;

    private byte[] iv;
    private byte[] counterBlock;
    private byte[] derivedPasswordVerifier;
    private byte[] saltBytes;

    public AesEncoder(char[] password, AesStrength aesKeyStrength) {
        if (password == null || password.length == 0) {
            throw new Zip4jException("input password is empty or null");
        }
        if (aesKeyStrength != AesStrength.KEY_STRENGTH_128 &&
                aesKeyStrength != AesStrength.KEY_STRENGTH_256) {
            throw new Zip4jException("Invalid AES key strength");
        }

        this.password = password;
        this.aesKeyStrength = aesKeyStrength;
        this.finished = false;
        counterBlock = new byte[AES_BLOCK_SIZE];
        iv = new byte[AES_BLOCK_SIZE];
        init();
    }

    private void init() {
        int keyLength = aesKeyStrength.getKeyLength();
        int macLength = aesKeyStrength.getMacLength();
        int saltLength = aesKeyStrength.getSaltLength();

        saltBytes = generateSalt(saltLength);
        byte[] keyBytes = deriveKey(saltBytes, password, keyLength, macLength);

        if (keyBytes == null || keyBytes.length != (keyLength + macLength + PASSWORD_VERIFIER_LENGTH)) {
            throw new Zip4jException("invalid key generated, cannot decrypt file");
        }

        byte[] aesKey = new byte[keyLength];
        byte[] macKey = new byte[macLength];
        derivedPasswordVerifier = new byte[PASSWORD_VERIFIER_LENGTH];

        System.arraycopy(keyBytes, 0, aesKey, 0, keyLength);
        System.arraycopy(keyBytes, keyLength, macKey, 0, macLength);
        System.arraycopy(keyBytes, keyLength + macLength, derivedPasswordVerifier, 0, PASSWORD_VERIFIER_LENGTH);

        aesEngine = new AesEngine(aesKey);
        mac = new MacBasedPRF("HmacSHA1");
        mac.init(macKey);
    }

    private byte[] deriveKey(byte[] salt, char[] password, int keyLength, int macLength) {
        try {
            PBKDF2Parameters p = new PBKDF2Parameters("HmacSHA1", "ISO-8859-1",
                    salt, 1000);
            PBKDF2Engine e = new PBKDF2Engine(p);
            byte[] derivedKey = e.deriveKey(password, keyLength + macLength + PASSWORD_VERIFIER_LENGTH);
            return derivedKey;
        } catch(Exception e) {
            throw new Zip4jException(e);
        }
    }

    public void encryptData(byte[] buff) {

        if (buff == null) {
            throw new Zip4jException("input bytes are null, cannot perform AES encrpytion");
        }
        encrypt(buff, 0, buff.length);
    }

    private static byte[] generateSalt(int size) {
        return new byte[] {
                (byte)0x3, (byte)0x58, (byte)0xC6, (byte)0x44, (byte)0x26,
                (byte)0x6, (byte)0x30, (byte)0xD2, (byte)0xEF, (byte)0x2B,
                (byte)0x2D, (byte)0x83, (byte)0x7B, (byte)0x5F, (byte)0xAC, (byte)0xCB };

//        if (size != 8 && size != 16) {
//            throw new Zip4jException("invalid salt size, cannot generate salt");
//        }
//
//        int rounds = 0;
//
//        if (size == 8)
//            rounds = 2;
//        if (size == 16)
//            rounds = 4;
//
//        byte[] salt = new byte[size];
//        for (int j = 0; j < rounds; j++) {
//            Random rand = new Random();
//            int i = rand.nextInt();
//            salt[0 + j * 4] = (byte)(i >> 24);
//            salt[1 + j * 4] = (byte)(i >> 16);
//            salt[2 + j * 4] = (byte)(i >> 8);
//            salt[3 + j * 4] = (byte)i;
//        }
//        return salt;
    }

    public byte[] getFinalMac() {
        byte[] rawMacBytes = mac.doFinal();
        byte[] macBytes = new byte[10];
        System.arraycopy(rawMacBytes, 0, macBytes, 0, 10);
        return macBytes;
    }

    public byte[] getDerivedPasswordVerifier() {
        return derivedPasswordVerifier;
    }

    public byte[] getSaltBytes() {
        return saltBytes;
    }


    @Override
    public void encrypt(byte[] buf, int offs, int len) {
        if (finished) {
            // A non 16 byte block has already been passed to encrypter
            // non 16 byte block should be the last block of compressed data in AES encryption
            // any more encryption will lead to corruption of data
            throw new Zip4jException("AES Encrypter is in finished state (A non 16 byte block has already been passed to encrypter)");
        }

        if (len % 16 != 0) {
            this.finished = true;
        }

        for (int j = offs; j < (offs + len); j += AES_BLOCK_SIZE) {
            loopCount = (j + AES_BLOCK_SIZE <= (offs + len)) ?
                        AES_BLOCK_SIZE : ((offs + len) - j);

            prepareBuffAESIVBytes(iv, nonce);
            aesEngine.processBlock(iv, counterBlock);

            for (int k = 0; k < loopCount; k++) {
                buf[j + k] = (byte)(buf[j + k] ^ counterBlock[k]);
            }

            mac.update(buf, j, loopCount);
            nonce++;
        }
    }

    @Override
    public void writeHeader(@NonNull DataOutputStream out) throws IOException {
        out.writeBytes(saltBytes);
        out.writeBytes(derivedPasswordVerifier);
    }

    @Override
    public void close(DataOutputStream out) throws IOException {
        out.writeBytes(getFinalMac());
    }
}
