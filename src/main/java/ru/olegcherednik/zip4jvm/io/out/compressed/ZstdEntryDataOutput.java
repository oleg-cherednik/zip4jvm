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

import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.settings.CompressionLevelEnum;

import io.airlift.compress.Foo;
import io.airlift.compress.zstd.ZstdCompressor;
import io.airlift.compress.zstd.stream.ZstdCompressorNew;

import java.io.ByteArrayOutputStream;

/**
 * Compresses an entry using the pure-java {@link ZstdCompressor}. That compressor
 * is a one-shot (whole buffer) API, so the uncompressed data is accumulated in
 * memory and turned into a single zstd frame on {@link #close()}.
 * <p>
 * Note: {@link ZstdCompressor} always uses the default compression level, so
 * {@code compressionLevel} does not affect the produced frame.
 *
 * @author Oleg Cherednik
 * @since 07.11.2021
 */
final class ZstdEntryDataOutput extends CompressedEntryDataOutput {

    private final ByteArrayOutputStream buf = new ByteArrayOutputStream();

    ZstdEntryDataOutput(DataOutput out, CompressionLevelEnum compressionLevel) {
        super(out);
    }

    // ---------- OutputStream ----------

    @Override
    public void write(int b) {
        buf.write(b);
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() {
        byte[] uncompressed = buf.toByteArray();
        ZstdCompressorNew compressor = new ZstdCompressorNew();
        Foo foo = new Foo(compressor.maxCompressedLength(uncompressed.length));
        int len = compressor.compress(uncompressed, 0, uncompressed.length, foo);
        out.write(foo.getCompressed(), 0, len);
        super.close();
    }

}
