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
package ru.olegcherednik.zip4jvm.model;

import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;

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

    AE_1(1, "AE-1"),
    AE_2(2, "AE-2"),
    UNKNOWN(PkwareExtraField.NO_DATA, "AE-x");

    private final int number;
    private final String title;

    // @NotNull
    public static AesVersion parseNumber(int number) {
        for (AesVersion aesVersion : values())
            if (aesVersion.number == number)
                return aesVersion;

        return UNKNOWN;
    }

}
