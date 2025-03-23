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

import ru.olegcherednik.zip4jvm.model.EncryptionMethod;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 22.09.2019
 */
@Test
public class WinZipAesFactoryTest {

    public void shouldRetrieveCorrectEncryptionWhenAesStrength() {
        assertThat(EncryptionMethod.of(AesStrength.NULL)).isSameAs(EncryptionMethod.OFF);
        assertThat(EncryptionMethod.of(AesStrength.S128)).isSameAs(EncryptionMethod.AES_128);
        assertThat(EncryptionMethod.of(AesStrength.S192)).isSameAs(EncryptionMethod.AES_192);
        assertThat(EncryptionMethod.of(AesStrength.S256)).isSameAs(EncryptionMethod.AES_256);
    }

}
