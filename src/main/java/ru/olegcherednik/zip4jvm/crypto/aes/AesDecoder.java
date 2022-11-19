/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.crypto.aes;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Objects;

import static ru.olegcherednik.zip4jvm.crypto.aes.AesEngine.MAC_SIZE;
import static ru.olegcherednik.zip4jvm.crypto.aes.AesEngine.PASSWORD_CHECKSUM_SIZE;

/**
 * @author Oleg Cherednik
 * @since 13.08.2019
 */
public final class AesDecoder implements Decoder {

    private static final int SHA1_NUM_DIGEST_WORDS = 5;
    private static final int SHA1_DIGEST_SIZE = SHA1_NUM_DIGEST_WORDS * 4;

    private static final int kDigestSize = SHA1_DIGEST_SIZE;

    private final int saltLength;
    private final AesEngine engine;

    public static AesDecoder create(ZipEntry zipEntry, DataInput in) throws IOException {
        try {
            if (zipEntry.isStrongEncryption()) {
                DecryptionHeader decryptionHeader = new DecryptionHeaderReader().read(in);
                // decryptionHeader.getVersion() should be 3

                int algId = decryptionHeader.getEncryptionAlgorithmCode();
                algId -= EncryptionAlgorithm.AES_128.getCode();

                AesStrength strength = AesStrength.S256;

                byte[] masterKey = getMasterKey(zipEntry.getPassword());
                int _keyKeySize = 16 + algId * 8;  //32 strength.keyLength()

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                Key key = new SecretKeySpec(masterKey, "AES");
                cipher.init(Cipher.DECRYPT_MODE, key);
                byte[] encryptedRandomData = decryptionHeader.getEncryptedRandomData();
                byte[] decrypted = cipher.doFinal(encryptedRandomData);


                byte[] fileKey = getFileKey(decryptionHeader.getIv());

                int rdSize = decryptionHeader.getEncryptedRandomData().length;
                int kPadSize = 16;

                if (rdSize < kPadSize)
                    throw new RuntimeException();
                if ((rdSize & (kPadSize - 1)) != 0)
                    throw new RuntimeException();

//                String passwordSha1 = DigestUtils.sha1Hex(new String(zipEntry.getPassword()));
//                byte[] _keyMasterKey = DeriveKey(passwordSha1);

                byte[] iv = decryptionHeader.getIv();

//                strength = AesEngine.getStrength(zipEntry.getEncryptionMethod());
//                byte[] salt = iv;
//                key = AesEngine.createKey(zipEntry.getPassword(), salt, strength);

//                Cipher cipher = AesEngine.createCipher(strength.createSecretKeyForCipher(key));
//                Mac mac = AesEngine.createMac(strength.createSecretKeyForMac(key));

                return null;
            } else {
                AesStrength strength = AesEngine.getStrength(zipEntry.getEncryptionMethod());
                byte[] salt = in.readBytes(strength.saltLength());
                byte[] key = AesEngine.createKey(zipEntry.getPassword(), salt, strength);

                Cipher cipher = AesEngine.createCipher(strength.createSecretKeyForCipher(key));
                Mac mac = AesEngine.createMac(strength.createSecretKeyForMac(key));
                byte[] passwordChecksum = strength.createPasswordChecksum(key);

                checkPasswordChecksum(passwordChecksum, zipEntry, in);

                return new AesDecoder(cipher, mac, salt.length);
            }
        } catch(Zip4jvmException | IOException e) {
            throw e;
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    public static byte[] getFileKey(byte[] iv) {
        byte[] sha1 = DigestUtils.sha1(iv);
        return null;
    }

    public static byte[] getMasterKey(char[] password) {
        byte[] data = new String(password).getBytes(StandardCharsets.UTF_8);
        byte[] sha1 = DigestUtils.sha1(data);
        return DeriveKey(sha1);
    }

    private static byte[] DeriveKey(byte[] digest) {
        byte[] buf = new byte[kDigestSize * 2];  // kDigestSize = 20
        DeriveKey2(digest, (byte)0x36, buf, 0);
        DeriveKey2(digest, (byte)0x5C, buf, kDigestSize);
        return Arrays.copyOfRange(buf, 0, 32);
    }

    private static void DeriveKey2(byte[] digest, byte c, byte[] dest, int offs) {
        byte[] buf = new byte[64];
        Arrays.fill(buf, c);

        for (int i = 0; i < kDigestSize; i++)
            buf[i] ^= digest[i];

        byte[] sha1 = DigestUtils.sha1(buf);
        System.arraycopy(sha1, 0, dest, offs, sha1.length);
    }

//    private static byte[] DeriveKey(byte[] digest) {
//        byte[] sha1lo = DeriveKey2(digest, (byte)0x36);
//        byte[] sha1hi = DeriveKey2(digest, (byte)0x5C);
//        return ArrayUtils.addAll(sha1lo, sha1hi);
//    }
//
//    private static byte[] DeriveKey2(byte[] digest, byte b) {
//        byte[] data = new byte[64];
//        Arrays.fill(data, b);
//
//        for (int i = 0; i < 20; i++)
//            data[i] ^= digest[i];
//
//        return DigestUtils.sha1(data);
//    }

    private AesDecoder(Cipher cipher, Mac mac, int saltLength) {
        this.saltLength = saltLength;
        engine = new AesEngine(cipher, mac);
    }

    @Override
    public void decrypt(byte[] buf, int offs, int len) {
        try {
            engine.updateMac(buf, offs, len);
            engine.cypherUpdate(buf, offs, len);
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    @Override
    public long getDataCompressedSize(long compressedSize) {
        return AesEngine.getDataCompressedSize(compressedSize, saltLength);
    }

    @Override
    public void close(DataInput in) throws IOException {
        checkMessageAuthenticationCode(in);
    }

    private void checkMessageAuthenticationCode(DataInput in) throws IOException {
        byte[] expected = in.readBytes(MAC_SIZE);
        byte[] actual = ArrayUtils.subarray(engine.getMac(), 0, MAC_SIZE);

        if (!ArrayUtils.isEquals(expected, actual))
            throw new Zip4jvmException("Message Authentication Code (MAC) is incorrect");
    }

    private static void checkPasswordChecksum(byte[] actual, ZipEntry entry, DataInput in) throws IOException {
        byte[] expected = in.readBytes(PASSWORD_CHECKSUM_SIZE);

        if (!Objects.deepEquals(expected, actual))
            throw new IncorrectPasswordException(entry.getFileName());
    }

}
