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

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.utils.ReflectionUtils;

import javax.crypto.Cipher;
import javax.crypto.Mac;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Oleg Cherednik
 * @since 22.09.2019
 */
@Test
public class AesEngineTest {

    public void shouldRetrieveCorrectStrengthWhenEncryption() {
        for (EncryptionMethod encryptionMethod : EncryptionMethod.values()) {
            if (encryptionMethod == EncryptionMethod.AES_128)
                assertThat(AesEngine.getStrength(EncryptionMethod.AES_128)).isSameAs(AesStrength.S128);
            else if (encryptionMethod == EncryptionMethod.AES_192)
                assertThat(AesEngine.getStrength(EncryptionMethod.AES_192)).isSameAs(AesStrength.S192);
            else if (encryptionMethod == EncryptionMethod.AES_256)
                assertThat(AesEngine.getStrength(EncryptionMethod.AES_256)).isSameAs(AesStrength.S256);
            else
                assertThat(AesEngine.getStrength(encryptionMethod)).isSameAs(AesStrength.NULL);
        }
    }

    public void shouldRetrieveCorrectEncryptionWhenAesStrength() {
        assertThat(AesEngine.getEncryption(AesStrength.NULL)).isSameAs(EncryptionMethod.OFF);
        assertThat(AesEngine.getEncryption(AesStrength.S128)).isSameAs(EncryptionMethod.AES_128);
        assertThat(AesEngine.getEncryption(AesStrength.S192)).isSameAs(EncryptionMethod.AES_192);
        assertThat(AesEngine.getEncryption(AesStrength.S256)).isSameAs(EncryptionMethod.AES_256);
    }

    public void shouldUpdateIv() throws Throwable {
        AesEngine engine = new AesEngine(mock(Cipher.class), mock(Mac.class));
        assertThat(getIv(engine)).isEqualTo(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

        ivUpdate(engine);
        assertThat(getIv(engine)).isEqualTo(new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

        ivUpdate(engine);
        assertThat(getIv(engine)).isEqualTo(new byte[] { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

        setIv(engine, new byte[] { -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
        ivUpdate(engine);
        assertThat(getIv(engine)).isEqualTo(new byte[] { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

        setIv(engine, new byte[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 });
        ivUpdate(engine);
        assertThat(getIv(engine)).isEqualTo(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
    }

    private static byte[] getIv(AesEngine engine) throws NoSuchFieldException, IllegalAccessException {
        return ReflectionUtils.getFieldValue(engine, "iv");
    }

    private static void ivUpdate(AesEngine engine) throws Throwable {
        ReflectionUtils.invokeMethod(engine, "ivUpdate");
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private static void setIv(AesEngine engine, byte[] iv) throws NoSuchFieldException, IllegalAccessException {
        ReflectionUtils.setFieldValue(engine, "iv", iv);
    }
}
