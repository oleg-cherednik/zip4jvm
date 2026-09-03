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
import ru.olegcherednik.zip4jvm.model.settings.CompressionLevelEnum;

import io.airlift.compress.zstd.ZstdCompressor;

import java.io.ByteArrayOutputStream;

/**
 * @author Oleg Cherednik
 * @since 07.11.2021
 */
final class ZstdEntryDataOutput extends CompressedEntryDataOutput {

    //    private final ZstdOutputStream zstd;
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    ZstdEntryDataOutput(DataOutput out, CompressionLevelEnum compressionLevel) {
        super(out);

//        zstd = Quietly.doRuntime(() -> {
//            int level = compressionLevel(compressionLevel);
//            return new ZstdOutputStream(DataOutputOutputStream.createUnseasonable(out), level);
//        });
    }

    // ---------- WriteBuffer ----------

    @Override
    public void write(int b) {
        baos.write(b);
//        Quietly.doRuntime(() -> zstd.write(b));
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() {
        ZstdCompressor compressor = new ZstdCompressor();
        byte[] input = baos.toByteArray();
        byte[] output = new byte[compressor.maxCompressedLength(input.length)];
        int length = compressor.compress(input, 0, input.length, output, 0, output.length);
        out.write(output, 0, length);
//        Quietly.doRuntime(zstd::close);
        super.close();
    }

    // ---------- static ----------

//    private static int compressionLevel(CompressionLevelEnum compressionLevel) {
//        if (compressionLevel == CompressionLevelEnum.SUPER_FAST)
//            return 1;
//        if (compressionLevel == CompressionLevelEnum.FAST)
//            return 2;
//        if (compressionLevel == CompressionLevelEnum.NORMAL)
//            return 3;
//        if (compressionLevel == CompressionLevelEnum.MAXIMUM)
//            return 17;
//        return 3;
//    }

}
