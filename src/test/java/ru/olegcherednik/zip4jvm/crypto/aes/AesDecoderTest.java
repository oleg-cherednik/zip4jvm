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

import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.file.DataInputFile;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.ReflectionUtils;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 03.10.2019
 */
@Test
@SuppressWarnings("NewMethodNamingConvention")
public class AesDecoderTest {

    public void shouldThrowZip4jvmExceptionWhenCreateAndException() {
        ZipEntry entry = mock(ZipEntry.class);
        DataInput in = mock(DataInput.class);
        assertThatThrownBy(() -> AesDecoder.create(in, entry)).isExactlyInstanceOf(Zip4jvmException.class);
    }

    public void shouldThrowZip4jvmExceptionWhenDecryptAndException() throws ShortBufferException {
        AesDecoder decoder = createAesDecoder(new AesEngine(mock(Cipher.class), mock(Mac.class)), 3);
        assertThatThrownBy(() -> decoder.decrypt(ArrayUtils.EMPTY_BYTE_ARRAY, 0, 10)).isExactlyInstanceOf(Zip4jvmException.class);
    }

    public void shouldThrowExceptionWhenMessageAuthenticationCodeNotMatch() throws Exception {
        try (DataInputFile in = mock(DataInputFile.class)) {
            byte[] keyBytes = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
            SecretKeySpec key = new SecretKeySpec(keyBytes, "RawBytes");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);

            when(in.readBytes(any(int.class))).thenReturn(new byte[] { 1, 2, 3 });

            AesDecoder decoder = createAesDecoder(new AesEngine(mock(Cipher.class), mac), 3);
            assertThatThrownBy(() -> decoder.close(in)).isExactlyInstanceOf(Zip4jvmException.class);
        }
    }

    private static AesDecoder createAesDecoder(AesEngine engine, long compressedSize) {
        return ReflectionUtils.invokeConstructor(AesDecoder.class, new Class<?>[] { AesEngine.class, long.class }, engine, compressedSize);
    }

}
