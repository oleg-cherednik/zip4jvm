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
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.Flag;
import ru.olegcherednik.zip4jvm.crypto.strong.cipher.StrongCipherUtils;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.PureJavaCrc32;

import java.security.Key;
import java.security.SecureRandom;
import java.util.zip.Checksum;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

/**
 * Writes PKWARE "strong encryption" 3DES entries (see APPNOTE 7.2), i.e. the
 * encryption counterpart of {@link StrongTripleDesCipherFactory}. 3DES is a
 * block cipher operating in CBC mode, so this encoder buffers incoming bytes
 * into {@link #BLOCK_SIZE}-byte blocks and appends a final PKCS#5 padding block
 * on {@link #close(DataOutput)}.
 *
 * @author Oleg Cherednik
 * @since 24.07.2026
 */
@RequiredArgsConstructor
public final class StrongTripleDesEncoder implements Encoder {

    private static final int BLOCK_SIZE = 8;
    private static final int IV_SIZE = 16;
    private static final int RANDOM_DATA_SIZE = 128;
    /**
     * Password validation data ({@code VData}) followed by 4 bytes of {@code VCRC32}.
     */
    private static final int VALIDATION_DATA_SIZE = 128;
    private static final int VERSION = 3;

    private final EncryptionAlgorithm encryptionAlgorithm;
    private final int bitLength;
    private final byte[] iv;
    private final byte[] encryptedRandomData;
    private final byte[] passwordValidationData;
    private final Cipher cipher;

    private final byte[] block = new byte[BLOCK_SIZE];
    private int blockLen;

    @SuppressWarnings("NewMethodNamingConvention")
    public static StrongTripleDesEncoder tripleDes112(ZipEntry zipEntry) {
        return create(zipEntry, EncryptionAlgorithm.TRIPLE_DES_112, TripleDesStrength.S112);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static StrongTripleDesEncoder tripleDes168(ZipEntry zipEntry) {
        return create(zipEntry, EncryptionAlgorithm.TRIPLE_DES_168, TripleDesStrength.S168);
    }

    private static StrongTripleDesEncoder create(ZipEntry zipEntry,
                                                 EncryptionAlgorithm encryptionAlgorithm,
                                                 TripleDesStrength strength) {
        return Quietly.doRuntime(() -> {
            char[] password = zipEntry.getPassword();

            SecureRandom random = new SecureRandom();
            byte[] iv = new byte[IV_SIZE];
            byte[] randomData = new byte[RANDOM_DATA_SIZE];
            random.nextBytes(iv);
            random.nextBytes(randomData);

            IvParameterSpec ivParam = new IvParameterSpec(iv, 0, BLOCK_SIZE);
            byte[] encryptedRandomData = StrongCipherUtils.encryptRandomData(password, randomData, strength,
                                                                             "DESede/CBC/PKCS5Padding", ivParam);

            byte[] fileKey = StrongCipherUtils.getFileKey(iv, randomData, strength.getKeySize());
            Key key = strength.createSecretKeyForCipher(fileKey);
            Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParam);

            byte[] passwordValidationData = createPasswordValidationData(cipher, random);

            return new StrongTripleDesEncoder(encryptionAlgorithm, strength.getSize(), iv,
                                              encryptedRandomData, passwordValidationData, cipher);
        });
    }

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

    // ---------- Encoder ----------

    @Override
    public void writeEncryptionHeader(DataOutput out) {
        int size = 2 /* version */ + 2 /* algId */ + 2 /* bitLength */ + 2 /* flags */
                + 2 /* erdSize */ + encryptedRandomData.length
                + 4 /* recipientCount */
                + 2 /* vSize */ + passwordValidationData.length;

        out.writeWord(iv.length);
        out.writeBytes(iv);
        out.writeDword(size);

        out.writeWord(VERSION);
        out.writeWord(encryptionAlgorithm.getCode());
        out.writeWord(bitLength);
        out.writeWord(Flag.PASSWORD_KEY.getCode());
        out.writeWord(encryptedRandomData.length);
        out.writeBytes(encryptedRandomData);
        out.writeDword(0);
        out.writeWord(passwordValidationData.length);
        out.writeBytes(passwordValidationData);
    }

    // ---------- Encrypt ----------

    @Override
    public byte encrypt(byte b) {
        throw new UnsupportedOperationException("3DES is a block cipher; use writeEncrypted()");
    }

    @Override
    public void writeEncrypted(int b, DataOutput out) {
        block[blockLen++] = (byte) b;

        if (blockLen == BLOCK_SIZE) {
            out.writeBytes(Quietly.doRuntime(() -> cipher.update(block)));
            blockLen = 0;
        }
    }

    @Override
    public void close(DataOutput out) {
        int pad = BLOCK_SIZE - blockLen;

        for (int i = blockLen; i < BLOCK_SIZE; i++)
            block[i] = (byte) pad;

        out.writeBytes(Quietly.doRuntime(() -> cipher.doFinal(block)));
        blockLen = 0;
    }

}
