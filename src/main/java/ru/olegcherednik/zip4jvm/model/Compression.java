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

import ru.olegcherednik.zip4jvm.exception.CompressionNotSupportedException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Function;

/**
 * This matches with {@link  CompressionMethod}, but here we have only supported methods.
 *
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
@RequiredArgsConstructor
public enum Compression {

    STORE(CompressionMethod.STORE, null, "store"),
    DEFLATE(CompressionMethod.DEFLATE, null, "deflate"),
    ENHANCED_DEFLATE(CompressionMethod.ENHANCED_DEFLATE, null, "enhanced-deflate"),
    BZIP2(CompressionMethod.BZIP2, null, "bzip2"),
    LZMA(CompressionMethod.LZMA, null, "lzma"),
    ZSTD(CompressionMethod.ZSTD, null, "zstd");

    @Getter
    private final CompressionMethod method;
    private final Function<DataInput, DataInput> decoratorDataInput;
    @Getter
    private final String title;

    public DataInput addCompressionDecorator(DataInput in) {
        return Optional.ofNullable(decoratorDataInput)
                       .orElseThrow(() -> new CompressionNotSupportedException(this))
                       .apply(in);
    }

//    public static DataInput create(Zip64.ExtensibleDataSector extensibleDataSector, DataInput in) {
//        CompressionMethod compressionMethod = extensibleDataSector.getCompressionMethod();
//
//        if (compressionMethod == CompressionMethod.STORE)
//            return new StoreDataInput(in);
//        if (compressionMethod == CompressionMethod.DEFLATE)
//            return new InflateDataInput(in);
//        if (compressionMethod == CompressionMethod.ENHANCED_DEFLATE)
//            return new EnhancedDeflateDataInput(in);
//        if (compressionMethod == CompressionMethod.BZIP2)
//            return new Bzip2DataInput(in);
//
//        throw new CompressionNotSupportedException(compressionMethod);
//    }


    public static Compression parseCompressionMethod(CompressionMethod compressionMethod) {
        for (Compression compression : values())
            if (compression.method == compressionMethod)
                return compression;

        throw new CompressionNotSupportedException(compressionMethod);
    }

}
