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
package ru.olegcherednik.zip4jvm.model.settings;

import ru.olegcherednik.zip4jvm.model.Encryption;

/**
 * This is a list of encryption available for a user.
 *
 * @author Oleg Cherednik
 * @since 09.03.2019
 */
public enum EncryptionEnum {

    OFF,
    PKWARE,
    /**
     * {@link Encryption#AES_128} - WinZip AES 128 bit encryption
     */
    AES_128,
    /**
     * {@link Encryption#AES_192} - WinZip AES 192 bit encryption
     */
    AES_192,
    /**
     * {@link Encryption#AES_256} - WinZip AES 256 bit encryption
     */
    AES_256,
    TRIPLE_DES_168

}
