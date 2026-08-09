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

import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 11.12.2022
 */
@RequiredArgsConstructor
@SuppressWarnings("PMD.MethodReturnsInternalArray")
public final class SeparatePasswordProvider implements PasswordProvider {

    private final Map<String, char[]> fileNamePassword;
    private final char[] centralDirectoryPassword;

    @Override
    public char[] getFilePassword(String fileName) {
        return fileNamePassword.get(fileName);
    }

    @Override
    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
    public char[] getCentralDirectoryPassword() {
        return centralDirectoryPassword;
    }

}
