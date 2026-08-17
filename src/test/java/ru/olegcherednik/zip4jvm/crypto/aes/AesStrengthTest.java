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

import ru.olegcherednik.zip4jvm.model.Encryption;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 22.09.2019
 */
@Test
public class AesStrengthTest {

    public void shouldThrowExceptionWhenParseUnknownCode() {
        assertThatThrownBy(() -> AesStrength.of(-1)).isExactlyInstanceOf(EnumConstantNotPresentException.class);
    }

    public void shouldRetrieveCorrectStrengthWhenEncryption() {
        for (Encryption encryption : Encryption.values()) {
            if (encryption == Encryption.AES_128 || encryption == Encryption.AES_STRONG_128)
                assertThat(AesStrength.of(encryption)).isSameAs(AesStrength.S128);
            else if (encryption == Encryption.AES_192 || encryption == Encryption.AES_STRONG_192)
                assertThat(AesStrength.of(encryption)).isSameAs(AesStrength.S192);
            else if (encryption == Encryption.AES_256 || encryption == Encryption.AES_STRONG_256)
                assertThat(AesStrength.of(encryption)).isSameAs(AesStrength.S256);
            else
                assertThat(AesStrength.of(encryption)).isSameAs(AesStrength.NULL);
        }
    }

}
