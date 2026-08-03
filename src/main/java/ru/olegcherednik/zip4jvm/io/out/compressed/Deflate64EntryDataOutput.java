/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import ru.olegcherednik.zip4jvm.io.out.DataOutputOutputStream;
import ru.olegcherednik.zip4jvm.io.out.compressed.deflate64.Deflate64Compressor;
import ru.olegcherednik.zip4jvm.model.settings.CompressionLevelEnum;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

/**
 * Writes an entry using Deflate64 ("enhanced deflate", PKWARE method 9).
 * <p>
 * Incoming bytes are handed straight to {@link Deflate64Compressor}, which
 * accumulates them in a fixed size buffer and emits one deflate block per batch,
 * so the whole entry is never held in memory.
 *
 * @author Oleg Cherednik
 * @since 26.07.2026
 */
final class Deflate64EntryDataOutput extends CompressedEntryDataOutput {

    private final Deflate64Compressor compressor;

    Deflate64EntryDataOutput(DataOutput out, CompressionLevelEnum compressionLevel) {
        super(out);
        compressor = new Deflate64Compressor(DataOutputOutputStream.createUnseasonable(out),
                                             maxChain(compressionLevel));
    }

    // ---------- WriteBuffer ----------

    @Override
    public void write(int b) {
        Quietly.doRuntime(() -> compressor.write(b));
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() {
        Quietly.doRuntime(compressor::finish);
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
