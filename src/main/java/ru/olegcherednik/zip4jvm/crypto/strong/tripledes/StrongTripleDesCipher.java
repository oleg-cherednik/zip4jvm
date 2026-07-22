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
package ru.olegcherednik.zip4jvm.crypto.strong.tripledes;

import ru.olegcherednik.zip4jvm.crypto.strong.BaseStrongCipher;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

/**
 * PKWARE Strong Encryption (SES) cipher for the {@code 3DES} family.
 *
 * @author Oleg Cherednik
 * @since 21.07.2026
 */
public class StrongTripleDesCipher extends BaseStrongCipher {

    public static StrongTripleDesCipher getInstance(DecryptionHeader decryptionHeader,
                                                    char[] password,
                                                    TripleDesStrength strength) {
        return Quietly.doRuntime(() -> {
            IvParameterSpec iv = new IvParameterSpec(decryptionHeader.getIv(), 0, 8);
            byte[] randomData = decryptRandomData(decryptionHeader, password, strength, "DESede/CBC/PKCS5Padding", iv);
            byte[] fileKey = getFileKey(decryptionHeader, randomData, 24);
            Key key = strength.createSecretKeyForCipher(fileKey);

            Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, iv);

            return new StrongTripleDesCipher(cipher);
        });
    }

    protected StrongTripleDesCipher(Cipher cipher) {
        super(cipher);
    }

}
