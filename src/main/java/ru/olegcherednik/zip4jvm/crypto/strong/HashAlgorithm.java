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
package ru.olegcherednik.zip4jvm.crypto.strong;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 19.03.2020
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum HashAlgorithm {

    NONE(0x0, "none"),
    CRC32(0x1, "CRC32"),
    MD5(0x8003, "MD5"),
    SHA1(0x8004, "SHA1"),
    RIPEMD160(0x0807, "RIPEMD160"),
    SHA256(0x800C, "SHA256"),
    SHA384(0x800D, "SHA384"),
    SHA512(0x800E, "SHA512"),
    UNKNOWN(0xFFFF, "<unknown>");

    private final int code;
    private final String title;

    public static HashAlgorithm parseCode(int code) {
        for (HashAlgorithm hashAlgorithm : values())
            if (hashAlgorithm.code == code)
                return hashAlgorithm;

        return UNKNOWN;
    }
}
