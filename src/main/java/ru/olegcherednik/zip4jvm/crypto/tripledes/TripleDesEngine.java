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

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;

import javax.crypto.Cipher;

/**
 * @author Oleg Cherednik
 * @since 16.02.2020
 */
@RequiredArgsConstructor
public final class TripleDesEngine {

    public final Cipher cipher;

    public void cypherUpdate(byte[] buf, int offs, int len) {
        cipher.update(buf, offs, len);
    }

    public static TripleDesStrength getStrength(EncryptionMethod encryptionMethod) {
        if (encryptionMethod == EncryptionMethod.TRIPLE_DES_112)
            return TripleDesStrength.S112;
        if (encryptionMethod == EncryptionMethod.TRIPLE_DES_168)
            return TripleDesStrength.S168;
        return TripleDesStrength.NULL;
    }
}
