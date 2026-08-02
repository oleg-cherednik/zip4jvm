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
package ru.olegcherednik.zip4jvm.crypto.strong.desede;

import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.crypto.EncoderFactory;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.Flag;
import ru.olegcherednik.zip4jvm.crypto.strong.HashAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.cipher.StrongCipherUtils;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.PureJavaCrc32;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.zip.Checksum;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

/**
 * @author Oleg Cherednik
 * @since 01.08.2026
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class StrongTripleDesEncoderFactory implements EncoderFactory {

    private static final int IV_SIZE = 16;
    private static final int RANDOM_DATA_SIZE = 128;
    /**
     * Password validation data ({@code VData}) followed by 4 bytes of {@code VCRC32}.
     */
    private static final int VALIDATION_DATA_SIZE = 128;
    private static final int VERSION = 3;

    public static final StrongTripleDesEncoderFactory S112 = new StrongTripleDesEncoderFactory(TripleDesStrength.S112);
    public static final StrongTripleDesEncoderFactory S168 = new StrongTripleDesEncoderFactory(TripleDesStrength.S168);

    private final TripleDesStrength strength;

    // ---------- EncoderFactory ----------

    @Override
    public Encoder createEncoder(ZipEntry zipEntry) {
        return Quietly.doRuntime(() -> {
            char[] password = zipEntry.getPassword();
            byte[] iv = new byte[IV_SIZE];
            byte[] randomData = new byte[RANDOM_DATA_SIZE];

            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            random.nextBytes(randomData);

            IvParameterSpec ivParam = new IvParameterSpec(iv, 0, StrongTripleDesEncoder.BLOCK_SIZE);
            byte[] encryptedRandomData = StrongCipherUtils.encryptRandomData(password, randomData, strength,
                                                                             "DESede/CBC/PKCS5Padding", ivParam);

            byte[] fileKey = StrongCipherUtils.getFileKey(iv, randomData, strength.getKeySize());
            Key key = strength.createSecretKeyForCipher(fileKey);
            Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParam);

            byte[] passwordValidationData = createPasswordValidationData(cipher, random);

            DecryptionHeader decryptionHeader =
                    createDecryptionHeader(iv, zipEntry.getEncryption(), encryptedRandomData, passwordValidationData);

            return new StrongTripleDesEncoder(decryptionHeader, cipher);
        });
    }

    // ----------

    private DecryptionHeader createDecryptionHeader(byte[] iv,
                                                    Encryption encryption,
                                                    byte[] encryptedRandomData,
                                                    byte[] passwordValidationData) {
        DecryptionHeader decryptionHeader = new DecryptionHeader();
        decryptionHeader.setIv(iv);
        decryptionHeader.setVersion(VERSION);
        decryptionHeader.setEncryptionAlgorithm(EncryptionAlgorithm.parseEncryption(encryption).getCode());
        decryptionHeader.setBitLength(strength.getSize());
        decryptionHeader.setFlags(Flag.PASSWORD_KEY);
        decryptionHeader.setEncryptedRandomData(encryptedRandomData);
        decryptionHeader.setHashAlgorithm(HashAlgorithm.NONE.getCode());
        decryptionHeader.setRecipients(Collections.emptyList());
        decryptionHeader.setPasswordValidationData(passwordValidationData);

        return decryptionHeader;
    }

    // ---------- static ----------

    private static byte[] createPasswordValidationData(Cipher cipher, SecureRandom random) {
        byte[] buf = new byte[VALIDATION_DATA_SIZE];
        random.nextBytes(buf);

        Checksum crc = new PureJavaCrc32();
        crc.update(buf, 0, buf.length - 4);
        long value = crc.getValue();

        buf[buf.length - 4] = (byte) value;
        buf[buf.length - 3] = (byte) (value >> 8);
        buf[buf.length - 2] = (byte) (value >> 16);
        buf[buf.length - 1] = (byte) (value >> 24);

        return Quietly.doRuntime(() -> cipher.update(buf));
    }

}
