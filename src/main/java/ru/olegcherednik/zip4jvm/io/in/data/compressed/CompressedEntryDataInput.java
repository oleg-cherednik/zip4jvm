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
package ru.olegcherednik.zip4jvm.io.in.data.compressed;

import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.exception.CompressionNotSupportedException;
import ru.olegcherednik.zip4jvm.io.in.data.BaseDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.ecd.EnhancedDeflateDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.ecd.InflateDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.ecd.StoreDataInput;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * This stream is responsible to read {@link ZipEntry} data. It could be encrypted; therefore all read data should be go
 * throw given {@link Decoder}.
 *
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
public abstract class CompressedEntryDataInput extends BaseDataInput {

    public static DataInput create(ZipEntry zipEntry,
                                   Function<Charset, Charset> chrsetCustomizer,
                                   DataInput in) {
        CompressionMethod compressionMethod = zipEntry.getCompressionMethod();
        // TODO we should check max available bytes

        if (compressionMethod == CompressionMethod.STORE)
            return new StoreDataInput(in);
        if (compressionMethod == CompressionMethod.DEFLATE)
            return new InflateDataInput(in);
        if (compressionMethod == CompressionMethod.ENHANCED_DEFLATE)
            return new EnhancedDeflateDataInput(in);
        if (compressionMethod == CompressionMethod.BZIP2)
            return new Bzip2EntryDataInput(in);
        if (compressionMethod == CompressionMethod.LZMA)
            return new LzmaEntryDataInput(zipEntry, in);
        if (compressionMethod == CompressionMethod.ZSTD)
            return new ZstdEntryDataInput(zipEntry, in);

        throw new CompressionNotSupportedException(compressionMethod);
    }

    protected CompressedEntryDataInput(DataInput in) {
        super(in);
    }

}
