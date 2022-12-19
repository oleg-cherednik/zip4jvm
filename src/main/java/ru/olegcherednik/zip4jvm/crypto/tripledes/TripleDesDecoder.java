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
package ru.olegcherednik.zip4jvm.crypto.tripledes;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * @author Oleg Cherednik
 * @since 16.02.2020
 */
public final class TripleDesDecoder implements Decoder {

    private final TripleDesEngine engine;
    private final long decryptionHeaderSize;
    @Getter
    private final long compressedSize = 0;

    public static TripleDesDecoder create(ZipEntry zipEntry, DataInput in) throws IOException {
        try {
            in.mark("bb");
            DecryptionHeader decryptionHeader = new DecryptionHeaderReader().read(in);
            byte[] iv = decryptionHeader.getIv();
            byte[] ivOne = Arrays.copyOfRange(iv, 0, 8);
            byte[] ivTwo = Arrays.copyOfRange(iv, 8, 16);
            //168
            int bitLength = decryptionHeader.getBitLength();
            byte[] salt = decryptionHeader.getEncryptedRandomData();
            String psw = "ThisIsSecretEncryptionKey";
//            byte[] sha1 = DigestUtils.sha1(psw);

            byte[] pvd = decryptionHeader.getPasswordValidationData();
            byte[] pvd_salt = Arrays.copyOfRange(pvd, 0, 4);
            byte[] pvd_crc32 = Arrays.copyOfRange(pvd, pvd.length - 4, pvd.length);

            Checksum checksum = new CRC32();
            checksum.update(pvd, 0, 4);
//            long crc32 = checksum.getValue();


            KeySpec keySpec = new PBEKeySpec(psw.toCharArray(), salt, 100, bitLength);
            byte[] key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(keySpec).getEncoded();
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, 0, bitLength / 8, "DESede");
            Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);


            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivOne);
            //            byte[] password = DigestUtils.sha1("".getBytes(StandardCharsets.UTF_8));
            byte[] password = psw.getBytes(StandardCharsets.UTF_8);

            return new TripleDesDecoder(cipher, in.getAbsoluteOffs() - in.getMark("bb"));
        } catch(Exception e) {
            throw new IOException(e);
        }
    }

    private TripleDesDecoder(Cipher cipher, long decryptionHeaderSize) {
        engine = new TripleDesEngine(cipher);
        this.decryptionHeaderSize = decryptionHeaderSize;
    }

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        assert len > 0;

        try {
            byte[] plain = engine.cipher.doFinal(buf, offs, len);
            String str = new String(plain, StandardCharsets.UTF_8);
            System.out.println(str);
            int a = 0;
            a++;
            return len;
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
//        engine.cypherUpdate(buf, offs, len);
    }

    /*
    public String encryptText(String plainText) throws Exception {
    // ---- Use specified 3DES key and IV from other source --------------
    byte[] plaintext = plainText.getBytes();
    byte[] tdesKeyData = Config.key.getBytes();
    // byte[] myIV = initializationVector.getBytes();
    Cipher c3des = Cipher.getInstance("DESede/CBC/PKCS5Padding");
    SecretKeySpec myKey = new SecretKeySpec(tdesKeyData, "DESede");
    IvParameterSpec ivspec = new IvParameterSpec(Config.initializationVector.getBytes());
    c3des.init(Cipher.ENCRYPT_MODE, myKey, ivspec);
    byte[] cipherText = c3des.doFinal(plaintext);
    return new BASE64Encoder().encode(cipherText);
}

public static String decryptText(String cipherText) throws Exception {
    byte[] encData = new BASE64Decoder().decodeBuffer(cipherText);
    Cipher decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
    byte[] tdesKeyData = Config.key.getBytes();
    SecretKeySpec myKey = new SecretKeySpec(tdesKeyData, "DESede");
    IvParameterSpec ivspec = new IvParameterSpec(Config.initializationVector.getBytes());
    decipher.init(Cipher.DECRYPT_MODE, myKey, ivspec);
    byte[] plainText = decipher.doFinal(encData);
    return new String(plainText);
}
     */
}
