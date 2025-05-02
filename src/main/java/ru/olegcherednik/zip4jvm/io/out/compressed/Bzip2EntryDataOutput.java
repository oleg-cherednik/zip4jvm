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
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 12.04.2020
 */
final class Bzip2EntryDataOutput extends CompressedEntryDataOutput {

    private final OutputStream bzip2;

    Bzip2EntryDataOutput(DataOutput out, CompressionLevelEnum compressionLevel) {
        super(out);
        bzip2 = Quietly.doRuntime(() -> new BZip2CompressorOutputStream(out, blockSize(compressionLevel)));
    }

    // ---------- OutputStream ----------

    @Override
    public void write(int b) throws IOException {
        bzip2.write(b);
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        bzip2.close();
        super.close();
    }

    // ---------- static ----------

    private static int blockSize(CompressionLevelEnum compressionLevel) {
        if (compressionLevel == CompressionLevelEnum.SUPER_FAST)
            return 1;
        if (compressionLevel == CompressionLevelEnum.FAST)
            return 3;
        if (compressionLevel == CompressionLevelEnum.NORMAL)
            return 6;
        if (compressionLevel == CompressionLevelEnum.MAXIMUM)
            return 9;
        return 6;
    }

}
