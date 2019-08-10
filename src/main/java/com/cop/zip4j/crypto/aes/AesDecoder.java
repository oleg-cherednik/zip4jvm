package com.cop.zip4j.crypto.aes;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.crypto.aes.pbkdf2.MacBasedPRF;
import com.cop.zip4j.crypto.aes.pbkdf2.PBKDF2Engine;
import com.cop.zip4j.crypto.aes.pbkdf2.PBKDF2Parameters;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.aes.AesExtraDataRecord;
import com.cop.zip4j.model.aes.AesStrength;
import lombok.NonNull;
import lombok.Setter;

import java.util.Arrays;
import java.util.zip.ZipException;

import static com.cop.zip4j.crypto.aes.AesCipherUtil.prepareBuffAESIVBytes;
import static com.cop.zip4j.crypto.aes.AesEngine.AES_BLOCK_SIZE;

public class AesDecoder implements Decoder {

    public static final int PASSWORD_VERIFIER_LENGTH = 2;

    private AesExtraDataRecord aesExtraDataRecord;
    private char[] password;
    private AesEngine aesEngine;
    private MacBasedPRF mac;

    private int nonce = 1;
    private byte[] iv;
    private byte[] counterBlock;

    @Setter
    private byte[] macKey;
    private int saltLength;

    public int getSaltLength() {
        return saltLength;
    }

    public int getPasswordVerifierLength() {
        return PASSWORD_VERIFIER_LENGTH;
    }

    public AesDecoder(AesExtraDataRecord aesExtraDataRecord, char[] password, byte[] salt, byte[] passwordVerifier) throws ZipException {
        this.aesExtraDataRecord = aesExtraDataRecord;
        this.password = password;
        this.saltLength = salt.length;
        iv = new byte[AES_BLOCK_SIZE];
        counterBlock = new byte[AES_BLOCK_SIZE];
        init(salt, passwordVerifier);
    }

    private void init(byte[] salt, byte[] passwordVerifier) {
        AesStrength aesKeyStrength = aesExtraDataRecord.getStrength();

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

        aesEngine = new AesEngine(aesKey);
        mac = new MacBasedPRF("HmacSHA1");
        mac.init(macKey);
    }

    @Override
    public void decrypt(byte[] buf, int offs, int len) {
        for (int j = offs; j < (offs + len); j += AES_BLOCK_SIZE) {
            int loopCount = (j + AES_BLOCK_SIZE <= (offs + len)) ?
                            AES_BLOCK_SIZE : ((offs + len) - j);

            mac.update(buf, j, loopCount);
            prepareBuffAESIVBytes(iv, nonce);
            aesEngine.processBlock(iv, counterBlock);

            for (int k = 0; k < loopCount; k++) {
                buf[j + k] = (byte)(buf[j + k] ^ counterBlock[k]);
            }

            nonce++;
        }
    }

    @Override
    public int getLen(long bytesRead, int len, long length) {
        return bytesRead + len < length && len % 16 != 0 ? len - len % 16 : len;
    }

    @Override
    public void checkChecksum(@NonNull CentralDirectory.FileHeader fileHeader, long checksum) {
        byte[] actual = new byte[AesEngine.AES_AUTH_LENGTH];
        System.arraycopy(mac.doFinal(), 0, actual, 0, actual.length);

        if (!Arrays.equals(actual, macKey))
            throw new Zip4jException("invalid CRC (MAC) for file '" + fileHeader.getFileName() + '\'');
    }

    private static byte[] deriveKey(byte[] salt, char[] password, int keyLength, int macLength) {
        PBKDF2Parameters p = new PBKDF2Parameters("HmacSHA1", "ISO-8859-1", salt, 1000);
        PBKDF2Engine e = new PBKDF2Engine(p);
        return e.deriveKey(password, keyLength + macLength + PASSWORD_VERIFIER_LENGTH);
    }

    @Override
    public long getCompressedSize(@NonNull LocalFileHeader localFileHeader) {
        return localFileHeader.getCompressedSize() - getSaltLength() - getPasswordVerifierLength() - 10;
    }

    @Override
    public long getOffs(@NonNull LocalFileHeader localFileHeader) {
        // TODO why don;t have MAC SIZE
        return localFileHeader.getOffs() + getSaltLength() + getPasswordVerifierLength(); // + MAC SIZE
    }

}
