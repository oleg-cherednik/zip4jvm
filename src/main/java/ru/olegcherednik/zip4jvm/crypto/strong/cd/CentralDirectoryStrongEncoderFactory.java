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
package ru.olegcherednik.zip4jvm.crypto.strong.cd;

import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.crypto.strong.desede.StrongTripleDesEncoder;
import ru.olegcherednik.zip4jvm.exception.EncryptionNotSupportedException;
import ru.olegcherednik.zip4jvm.model.Encryption;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Creates an {@link Encoder} that encrypts the central directory with the given
 * password based {@link Encryption}. This is the encryption counterpart of
 * {@link CentralDirectoryStrongDecoderFactory}.
 *
 * @author Oleg Cherednik
 * @since 30.07.2026
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CentralDirectoryStrongEncoderFactory {

    public static final CentralDirectoryStrongEncoderFactory INSTANCE = new CentralDirectoryStrongEncoderFactory();

    // @NotNull
    public Encoder createEncoder(char[] password, Encryption encryption) {
        if (encryption == Encryption.TRIPLE_DES_168)
            return StrongTripleDesEncoder.tripleDes168(password);

        throw new EncryptionNotSupportedException(encryption);
    }

}
