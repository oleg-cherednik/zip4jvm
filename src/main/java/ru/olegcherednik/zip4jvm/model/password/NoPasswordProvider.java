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
package ru.olegcherednik.zip4jvm.model.password;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 10.12.2022
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
public final class NoPasswordProvider implements PasswordProvider {

    public static final PasswordProvider INSTANCE = new NoPasswordProvider();

    @Override
    public char[] getFilePassword(String fileName) {
        return null;
    }

    @Override
    public char[] getCentralDirectoryPassword() {
        return null;
    }
}
