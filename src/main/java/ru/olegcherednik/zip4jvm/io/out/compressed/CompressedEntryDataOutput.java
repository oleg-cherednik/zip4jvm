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
package ru.olegcherednik.zip4jvm.io.out.compressed;

import ru.olegcherednik.zip4jvm.exception.CompressionNotSupportedException;
import ru.olegcherednik.zip4jvm.io.out.BaseDataOutput;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.settings.CompressionLevelEnum;

/**
 * This class represents a compressed {@link DataOutput} stream using given
 * {@link Compression}. It extends from the {@link BaseDataOutput}.
 *
 * @author Oleg Cherednik
 * @since 12.02.2020
 */
public class CompressedEntryDataOutput extends BaseDataOutput {

    public static DataOutput create(ZipEntry entry, DataOutput out) {
        return create(entry.getCompression(), entry.getCompressionLevel(), entry.isLzmaEosMarker(), out);
    }

    public static DataOutput create(Compression compression,
                                    CompressionLevelEnum compressionLevel,
                                    boolean lzmaEosMarker,
                                    DataOutput out) {
        if (compression == Compression.STORE)
            return new StoreEntryDataOutput(out);
        if (compression == Compression.DEFLATE)
            return new DeflateEntryDataOutput(out, compressionLevel);
        if (compression == Compression.BZIP2)
            return new Bzip2EntryDataOutput(out, compressionLevel);
        if (compression == Compression.LZMA)
            return new LzmaEntryDataOutput(out, compressionLevel, lzmaEosMarker);
        if (compression == Compression.ZSTD)
            return new ZstdEntryDataOutput(out, compressionLevel);

        throw new CompressionNotSupportedException(compression);
    }

    protected CompressedEntryDataOutput(DataOutput out) {
        super(out);
    }

    // ---------- DataOutput ----------

    /*
     * All data primitives should be compressed as well; therefore they are
     * converted to the separate bytes and written using this stream instead of
     * delegating them to the not compressed one.
     */

    @Override
    public void writeByte(int val) {
        getByteOrder().writeByte(val, this);
    }

    @Override
    public void writeWord(int val) {
        getByteOrder().writeWord(val, this);
    }

    @Override
    public void writeDword(long val) {
        getByteOrder().writeDword(val, this);
    }

    @Override
    public void writeQword(long val) {
        getByteOrder().writeQword(val, this);
    }

}
