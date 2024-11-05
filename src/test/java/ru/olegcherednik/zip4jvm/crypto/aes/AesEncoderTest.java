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

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * @author Oleg Cherednik
 * @since 03.10.2019
 */
@Test
public class AesEncoderTest {

    public void shouldThrowZip4jvmExceptionWhenCreateAndException() {
        assertThatThrownBy(() -> AesEncoder.create(mock(ZipEntry.class))).isExactlyInstanceOf(Zip4jvmException.class);
    }

    //    public void shouldThrowZip4jvmExceptionWhenEncryptAndException() throws ShortBufferException {
    //        Cipher cipher = mock(Cipher.class);
    //        Mac mac = mock(Mac.class);
    //        byte[] salt = { 0, 0, 0 };
    //        byte[] passwordChecksum = { 0, 0, 0 };
    //
    //        AesEncoder encoder = createAesEncoder(cipher, mac, salt, passwordChecksum);
    //        assertThatThrownBy(() -> encoder.encrypt(ArrayUtils.EMPTY_BYTE_ARRAY, 0, 10)).isExactlyInstanceOf(
    //                Zip4jvmException.class);
    //    }

    //    private static AesEncoder createAesEncoder(Cipher cipher, Mac mac, byte[] salt, byte[] passwordChecksum) {
    //        return ReflectionUtils.invokeConstructor(AesEncoder.class,
    //                                                 new Class<?>[] { Cipher.class, Mac.class,
    //                                                 byte[].class, byte[].class },
    //                                                 cipher,
    //                                                 mac,
    //                                                 salt,
    //                                                 passwordChecksum);
    //    }
}
