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
package ru.olegcherednik.zip4jvm.model;

import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.model.settings.AesVersionEnum;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 09.11.2024
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum AesVersion {

    AE_1(1, AesVersionEnum.AE_1, "AE-1"),
    AE_2(2, AesVersionEnum.AE_2, "AE-2"),
    UNKNOWN(PkwareExtraField.NO_DATA, null, "AE-x");

    private final int code;
    @Getter(AccessLevel.NONE)
    private final AesVersionEnum aes;
    private final String title;

    // @NotNull
    public static AesVersion parseCode(int code) {
        for (AesVersion aesVersion : values())
            if (aesVersion.code == code)
                return aesVersion;

        return UNKNOWN;
    }

    // @NotNull
    public static AesVersion of(AesVersionEnum aes) {
        for (AesVersion aesVersion : values())
            if (aesVersion != UNKNOWN && aesVersion.aes == aes)
                return aesVersion;

        return AE_2;
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return title;
    }

}
