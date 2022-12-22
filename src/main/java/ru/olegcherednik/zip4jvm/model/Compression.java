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
import ru.olegcherednik.zip4jvm.exception.CompressionNotSupportedException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.buf.Bzip2DataInputNew;
import ru.olegcherednik.zip4jvm.io.in.buf.EnhancedDeflateDataInputNew;
import ru.olegcherednik.zip4jvm.io.in.buf.InflateDataInputNew;
import ru.olegcherednik.zip4jvm.io.in.buf.StoreBufferedDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputNew;

/**
 * This matches with {@link  CompressionMethod}, but here we have only supported methods.
 *
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
@RequiredArgsConstructor
public enum Compression {

    STORE(CompressionMethod.STORE, StoreBufferedDataInput::new),
    DEFLATE(CompressionMethod.DEFLATE, InflateDataInputNew::new),
    ENHANCED_DEFLATE(CompressionMethod.ENHANCED_DEFLATE, EnhancedDeflateDataInputNew::new),
    BZIP2(CompressionMethod.BZIP2, Bzip2DataInputNew::new),
    LZMA(CompressionMethod.LZMA, null),
    ZSTD(CompressionMethod.ZSTD, null);

    @Getter
    private final CompressionMethod method;
    private final CreateDataInputNew createDataInputNew;

    public DataInputNew createDataInput(DataInputNew in, int uncompressedSize) {
        if (createDataInputNew != null)
            return createDataInputNew.apply(in, uncompressedSize);
        throw new CompressionNotSupportedException(this);
    }

    public static Compression parseCompressionMethod(CompressionMethod compressionMethod) {
        for (Compression compression : values())
            if (compression.method == compressionMethod)
                return compression;

        throw new CompressionNotSupportedException(compressionMethod);
    }

    private interface CreateDataInputNew {

        DataInputNew apply(DataInputNew in, int uncompressedSize);
    }

}
