/*
 * Copyright 2010 Srikanth Reddy Lingala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lingala.zip4j.crypto;

import lombok.Getter;
import lombok.Setter;
import net.lingala.zip4j.crypto.PBKDF2.MacBasedPRF;
import net.lingala.zip4j.crypto.PBKDF2.PBKDF2Engine;
import net.lingala.zip4j.crypto.PBKDF2.PBKDF2Parameters;
import net.lingala.zip4j.crypto.engine.AESEngine;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.AESStrength;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.Raw;

import java.util.Random;

public class AESEncryptor implements Encryptor {

    private char[] password;
    private final AESStrength keyStrength;
    private AESEngine aesEngine;
    private MacBasedPRF mac;

    private int KEY_LENGTH;
    private int MAC_LENGTH;
    @Getter
    private int saltLength;
    @Getter
    private final int passwordVerifierLength = 2;

    private byte[] aesKey;
    private byte[] macKey;
    @Getter
    @Setter
    private byte[] derivedPasswordVerifier;
    @Getter
    @Setter
    private byte[] saltBytes;

    private boolean finished;

    private int nonce = 1;
    private int loopCount = 0;

    private byte[] iv;
    private byte[] counterBlock;

    public AESEncryptor(char[] password, AESStrength keyStrength) throws ZipException {
        if (password == null || password.length == 0)
            throw new ZipException("input password is empty or null in AES encrypter constructor");
        if (keyStrength != AESStrength.STRENGTH_128 && keyStrength != AESStrength.STRENGTH_256)
            throw new ZipException("Invalid key strength in AES encrypter constructor");

        this.password = password;
        this.keyStrength = keyStrength;
        this.finished = false;
        counterBlock = new byte[InternalZipConstants.AES_BLOCK_SIZE];
        iv = new byte[InternalZipConstants.AES_BLOCK_SIZE];
        init();
    }

    private void init() throws ZipException {
        if (keyStrength == AESStrength.STRENGTH_128) {
            KEY_LENGTH = 16;
            MAC_LENGTH = 16;
            saltLength = 8;
        } else if (keyStrength == AESStrength.STRENGTH_256) {
            KEY_LENGTH = 32;
            MAC_LENGTH = 32;
            saltLength = 16;
        } else
            throw new ZipException("invalid aes key strength, cannot determine key sizes");

        saltBytes = generateSalt(saltLength);
        byte[] keyBytes = deriveKey(saltBytes, password);

        if (keyBytes == null || keyBytes.length != (KEY_LENGTH + MAC_LENGTH + passwordVerifierLength)) {
            throw new ZipException("invalid key generated, cannot decrypt file");
        }

        aesKey = new byte[KEY_LENGTH];
        macKey = new byte[MAC_LENGTH];
        derivedPasswordVerifier = new byte[passwordVerifierLength];

        System.arraycopy(keyBytes, 0, aesKey, 0, KEY_LENGTH);
        System.arraycopy(keyBytes, KEY_LENGTH, macKey, 0, MAC_LENGTH);
        System.arraycopy(keyBytes, KEY_LENGTH + MAC_LENGTH, derivedPasswordVerifier, 0, passwordVerifierLength);

        aesEngine = new AESEngine(aesKey);
        mac = new MacBasedPRF("HmacSHA1");
        mac.init(macKey);
    }

    private byte[] deriveKey(byte[] salt, char[] password) throws ZipException {
        try {
            PBKDF2Parameters p = new PBKDF2Parameters("HmacSHA1", "ISO-8859-1",
                    salt, 1000);
            PBKDF2Engine e = new PBKDF2Engine(p);
            byte[] derivedKey = e.deriveKey(password, KEY_LENGTH + MAC_LENGTH + passwordVerifierLength);
            return derivedKey;
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    @Override
    public int encrypt(byte[] buf, int offs, int len) throws ZipException {

        if (finished) {
            // A non 16 byte block has already been passed to encrypter
            // non 16 byte block should be the last block of compressed data in AES encryption
            // any more encryption will lead to corruption of data
            throw new ZipException("AES Encrypter is in finished state (A non 16 byte block has already been passed to encrypter)");
        }

        if (len % 16 != 0) {
            this.finished = true;
        }

        for (int j = offs; j < (offs + len); j += InternalZipConstants.AES_BLOCK_SIZE) {
            loopCount = (j + InternalZipConstants.AES_BLOCK_SIZE <= (offs + len)) ?
                        InternalZipConstants.AES_BLOCK_SIZE : ((offs + len) - j);

            Raw.prepareBuffAESIVBytes(iv, nonce, InternalZipConstants.AES_BLOCK_SIZE);
            aesEngine.processBlock(iv, counterBlock);

            for (int k = 0; k < loopCount; k++)
                buf[j + k] = (byte)(buf[j + k] ^ counterBlock[k]);

            mac.update(buf, j, loopCount);
            nonce++;
        }

        return len;
    }

    private static byte[] generateSalt(int size) throws ZipException {

        if (size != 8 && size != 16) {
            throw new ZipException("invalid salt size, cannot generate salt");
        }

        int rounds = 0;

        if (size == 8)
            rounds = 2;
        if (size == 16)
            rounds = 4;

        byte[] salt = new byte[size];
        for (int j = 0; j < rounds; j++) {
            Random rand = new Random();
            int i = rand.nextInt();
            salt[0 + j * 4] = (byte)(i >> 24);
            salt[1 + j * 4] = (byte)(i >> 16);
            salt[2 + j * 4] = (byte)(i >> 8);
            salt[3 + j * 4] = (byte)i;
        }
        return salt;
    }

    public byte[] getFinalMac() {
        byte[] rawMacBytes = mac.doFinal();
        byte[] macBytes = new byte[10];
        System.arraycopy(rawMacBytes, 0, macBytes, 0, 10);
        return macBytes;
    }

}
