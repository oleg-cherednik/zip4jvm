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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
@Getter
@RequiredArgsConstructor
public enum Compression {
    STORE(CompressionMethod.STORE),
    DEFLATE(CompressionMethod.DEFLATE),
    ENHANCED_DEFLATE(CompressionMethod.ENHANCED_DEFLATE),
    BZIP2(CompressionMethod.BZIP2),
    LZMA(CompressionMethod.LZMA),
    ZSTD(CompressionMethod.ZSTD);

    private final CompressionMethod method;

    public static Compression parseCompressionMethod(CompressionMethod compressionMethod) {
        for (Compression compression : values())
            if (compression.method == compressionMethod)
                return compression;
        throw new EnumConstantNotPresentException(Compression.class, "compressionMethod=" + compressionMethod);
    }

}
