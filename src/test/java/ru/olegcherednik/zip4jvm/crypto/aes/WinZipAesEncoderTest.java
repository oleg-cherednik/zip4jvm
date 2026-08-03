/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.testng.annotations.Test;

/**
 * @author Oleg Cherednik
 * @since 03.10.2019
 */
@Test
public class WinZipAesEncoderTest {

    //    public void shouldThrowZip4jvmExceptionWhenCreateAndException() {
    //        assertThatThrownBy(() -> AesEncoder.create(mock(ZipEntry.class)))
    //        .isExactlyInstanceOf(Zip4jvmException.class);
    //    }

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
