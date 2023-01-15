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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.UnknownCompressionMethodException;

/**
 * see 4.4.5
 *
 * @author Oleg Cherednik
 * @since 09.03.2019
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum CompressionMethod {
    STORE(0, "none (stored)"),
    FILE_SHRUNK(1, "shrunk"),
    FILE_RED_COMP_FACTOR_1(2, "reduced (factor 1)"),
    FILE_RED_COMP_FACTOR_2(3, "reduced (factor 2)"),
    FILE_RED_COMP_FACTOR_3(4, "reduced (factor 3)"),
    FILE_RED_COMP_FACTOR_4(5, "reduced (factor 4)"),
    FILE_IMPLODED(6, "imploded"),
    DEFLATE(8, "deflate"),
    ENHANCED_DEFLATE(9, "deflate (enhanced)"),
    DCL_IMPLODE(10, "DCL Implode"),
    BZIP2(12, "bzip2 algorithm"),
    LZMA(14, "lzma encoding"),
    CMPSC(16, "IBM z/OS CMPSC Compression"),
    TERSE(18, "IBM TERSE"),
    LZ77(19, "IBM lz77 z Architecture"),
    ZSTD_OLD(20, "zstd compression (deprecated)"),
    ZSTD(93, "zstd compression"),
    MP3(94, "mp3 compression"),
    XZ(95, "xz compression"),
    JPEG(96, "jpeg compression"),
    WAVPACK(97, "wavpack compression"),
    PPMD(98, "ppmd encoding"),
    AES(99, "AES encryption");

    private final int code;
    private final String title;

    public static CompressionMethod parseCode(int code) {
        for (CompressionMethod method : values())
            if (method.code == code)
                return method;

        throw new UnknownCompressionMethodException(code);
    }
}
