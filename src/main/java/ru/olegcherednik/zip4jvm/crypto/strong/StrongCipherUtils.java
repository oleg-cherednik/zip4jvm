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
package ru.olegcherednik.zip4jvm.crypto.strong;

import ru.olegcherednik.zip4jvm.exception.IncorrectCentralDirectoryPasswordException;
import ru.olegcherednik.zip4jvm.io.ByteOrder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.Supplier;

/**
 * @author Oleg Cherednik
 * @since 22.07.2026
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StrongCipherUtils {

    public static <T extends StrongCipher> T validatePassword(Supplier<T> strongCipherSup,
                                                              DecryptionHeader decryptionHeader,
                                                              ByteOrder byteOrder) {
        T cipher = strongCipherSup.get();
        byte[] passwordValidationData = cipher.update(decryptionHeader.getPasswordValidationData());
        validatePasswordChecksum(passwordValidationData, byteOrder);
        return cipher;
    }

    public static void validatePasswordChecksum(byte[] passwordValidationData, ByteOrder byteOrder) {
        long actual = DecryptionHeader.getActualCrc32(passwordValidationData);
        long expected = DecryptionHeader.getExpectedCrc32(passwordValidationData, byteOrder);

        if (expected != actual)
            throw new IncorrectCentralDirectoryPasswordException();
    }

}
