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

import java.io.ByteArrayOutputStream;

/**
 * Writes an entry using Deflate64 ("enhanced deflate", PKWARE method 9).
 * <p>
 * This baseline encoder buffers the whole entry in memory and compresses it on
 * {@link #close()} using {@link Deflate64Compressor} (greedy LZ77 with
 * fixed Huffman / stored blocks and a 64 KB window). The result round-trips with
 * {@code Deflate64DataInput} on the read side.
 *
 * @author Oleg Cherednik
 * @since 26.07.2026
 */
final class Deflate64EntryDataOutput extends CompressedEntryDataOutput {

    private final ByteArrayOutputStream buf = new ByteArrayOutputStream();
    private final Deflate64StreamCompressor delegate;

    Deflate64EntryDataOutput(DataOutput out, CompressionLevelEnum compressionLevel) {
        super(out);
        delegate = new Deflate64StreamCompressor(maxChain(compressionLevel));
    }

    // ---------- WriteBuffer ----------

    @Override
    public void write(int b) {
        buf.write(b);
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() {
        byte[] compressed = delegate.compress(buf.toByteArray());
        out.write(compressed, 0, compressed.length);
        super.close();
    }

    // ---------- static ----------

    private static int maxChain(CompressionLevelEnum compressionLevel) {
        if (compressionLevel == CompressionLevelEnum.SUPER_FAST)
            return 8;
        if (compressionLevel == CompressionLevelEnum.FAST)
            return 32;
        if (compressionLevel == CompressionLevelEnum.NORMAL)
            return 128;
        if (compressionLevel == CompressionLevelEnum.MAXIMUM)
            return 1024;
        return 128;
    }

}
