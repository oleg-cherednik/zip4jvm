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
package ru.olegcherednik.zip4jvm.crypto.strong.aes;

import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.cipher.StrongCipher;
import ru.olegcherednik.zip4jvm.crypto.strong.cipher.StrongCipherFactory;
import ru.olegcherednik.zip4jvm.crypto.strong.cipher.StrongCipherUtils;
import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

/**
 * @author Oleg Cherednik
 * @since 22.07.2026
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StrongAesCipherFactory implements StrongCipherFactory {

    public static final StrongAesCipherFactory INSTANCE = new StrongAesCipherFactory();

    // ---------- StrongCipherFactory ----------

    @Override
    public StrongCipher createCipher(char[] password, DecryptionHeader decryptionHeader, ByteOrder byteOrder) {
        return Quietly.doRuntime(() -> {
            Encryption encryption = decryptionHeader.getEncryptionAlgorithm().getEncryption();
            AesStrength strength = AesStrength.of(encryption);
            assert strength != AesStrength.NULL;

            IvParameterSpec iv = new IvParameterSpec(decryptionHeader.getIv());
            byte[] randomData = StrongCipherUtils.decryptRandomData(password, decryptionHeader, strength,
                                                                    "AES/CBC/PKCS5Padding", iv);
            byte[] fileKey = StrongCipherUtils.getFileKey(decryptionHeader, randomData, 32);
            Key key = strength.createSecretKeyForCipher(fileKey);

            StrongCipher cipher = new StrongCipher(createCipher(key, iv));
            StrongCipherUtils.validatePassword(cipher, decryptionHeader, byteOrder);

            return cipher;
        });
    }

    // ----------

    private static Cipher createCipher(Key key, IvParameterSpec iv) {
        return Quietly.doRuntime(() -> {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            return cipher;
        });
    }

}
