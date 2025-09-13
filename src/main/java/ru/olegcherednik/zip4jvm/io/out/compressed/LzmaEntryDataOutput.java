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
import ru.olegcherednik.zip4jvm.io.out.decorators.UncloseableDataOutput;
import ru.olegcherednik.zip4jvm.model.settings.CompressionLevelEnum;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAOutputStream;

/**
 * @author Oleg Cherednik
 * @since 09.02.2020
 */
final class LzmaEntryDataOutput extends CompressedEntryDataOutput {

    private final LZMAOutputStream lzma;

    LzmaEntryDataOutput(DataOutput out, CompressionLevelEnum compressionLevel, boolean eosMarker) {
        super(out);

        lzma = Quietly.doRuntime(() -> {
            LZMA2Options options = new LZMA2Options(getPreset(compressionLevel));
            writeHeader(out, options);
            return new LZMAOutputStream(new UncloseableDataOutput(out), options, eosMarker);
        });
    }

    // ---------- OutputStream ----------

    @Override
    public void write(int b) {
        Quietly.doRuntime(() -> lzma.write(b));
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() {
        Quietly.doRuntime(lzma::close);
        super.close();
    }

    // ---------- static ----------

    private static int getPreset(CompressionLevelEnum compressionLevel) {
        if (compressionLevel == CompressionLevelEnum.SUPER_FAST)
            return LZMA2Options.PRESET_MIN;
        if (compressionLevel == CompressionLevelEnum.FAST)
            return 3;
        if (compressionLevel == CompressionLevelEnum.NORMAL)
            return LZMA2Options.PRESET_DEFAULT;
        if (compressionLevel == CompressionLevelEnum.MAXIMUM)
            return LZMA2Options.PRESET_MAX;
        return LZMA2Options.PRESET_DEFAULT;
    }

    private static void writeHeader(DataOutput out, LZMA2Options options) {
        out.writeByte((byte) 19);    // major version
        out.writeByte((byte) 0);     // minor version
        out.writeWord(5);            // header size
        out.writeByte((byte) ((options.getPb() * 5 + options.getLp()) * 9 + options.getLc()));
        out.writeDword(options.getDictSize());
    }

}
