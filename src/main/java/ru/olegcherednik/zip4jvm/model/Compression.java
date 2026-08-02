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

import ru.olegcherednik.zip4jvm.exception.CompressionReadingNotSupportedException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.compressed.Bzip2DataInput;
import ru.olegcherednik.zip4jvm.io.in.compressed.CompressedDataInput;
import ru.olegcherednik.zip4jvm.io.in.compressed.Deflate64DataInput;
import ru.olegcherednik.zip4jvm.io.in.compressed.DeflateDataInput;
import ru.olegcherednik.zip4jvm.io.in.compressed.LzmaDataInput;
import ru.olegcherednik.zip4jvm.io.in.compressed.StoreDataInput;
import ru.olegcherednik.zip4jvm.io.in.compressed.ZstdDataInput;
import ru.olegcherednik.zip4jvm.model.settings.CompressionEnum;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * see 4.4.5
 *
 * @author Oleg Cherednik
 * @since 09.03.2019
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public enum Compression {

    STORE(0, CompressionEnum.STORE, StoreDataInput::create, "none (stored)", "store"),
    FILE_SHRUNK(1, "shrunk"),
    FILE_RED_COMP_FACTOR_1(2, "reduced (factor 1)"),
    FILE_RED_COMP_FACTOR_2(3, "reduced (factor 2)"),
    FILE_RED_COMP_FACTOR_3(4, "reduced (factor 3)"),
    FILE_RED_COMP_FACTOR_4(5, "reduced (factor 4)"),
    FILE_IMPLODED(6, "imploded"),
    DEFLATE(8, CompressionEnum.DEFLATE, DeflateDataInput::create, "deflate", "deflate"),
    DEFLATE_64(9, CompressionEnum.DEFLATE_64, Deflate64DataInput::create, "deflate (enhanced)", "enhanced-deflate"),
    DCL_IMPLODE(10, "DCL Implode"),
    BZIP2(12, CompressionEnum.BZIP2, Bzip2DataInput::create, "bzip2 algorithm", "bzip2"),
    LZMA(14, CompressionEnum.LZMA, LzmaDataInput::create, "lzma encoding", "lzma"),
    CMPSC(16, "IBM z/OS CMPSC Compression"),
    TERSE(18, "IBM TERSE"),
    LZ77(19, "IBM lz77 z Architecture"),
    ZSTD_OLD(20, "zstd compression (deprecated)"),
    ZSTD(93, CompressionEnum.ZSTD, ZstdDataInput::create, "zstd compression", "zstd"),
    MP3(94, "mp3 compression"),
    XZ(95, "xz compression"),
    JPEG(96, "jpeg compression"),
    WAVPACK(97, "wavpack compression"),
    PPMD(98, "ppmd encoding"),
    AES(99, "AES encryption"),  // This is encryption, see Encryption.AES_128/AES_192/AES_256
    UNKNOWN(-1, "<unknown>");

    private final int code;
    @Getter(AccessLevel.NONE)
    private final CompressionEnum comp;
    @Getter(AccessLevel.NONE)
    private final CompressionDecoratorFactory compressionDecoratorFactory;
    private final String title;
    private final String fileMarker;

    Compression(int code, String title) {
        this(code, null, null, title, null);
    }

    public CompressedDataInput addCompressionDecorator(DataInput in) {
        return Optional.ofNullable(compressionDecoratorFactory)
                       .orElseThrow(() -> new CompressionReadingNotSupportedException(this))
                       .create(in);
    }

    // ---------- static ----------

    // @NotNull
    public static Compression parseCode(int code) {
        for (Compression method : values())
            if (method.code == code)
                return method;

        return UNKNOWN;
    }

    // @NotNull
    public static Compression of(CompressionEnum comp) {
        for (Compression compression : values())
            if (compression != UNKNOWN && compression.comp == comp)
                return compression;

        return UNKNOWN;
    }

    // ---------- inner ----------

    private interface CompressionDecoratorFactory {

        CompressedDataInput create(DataInput in);

    }
}
