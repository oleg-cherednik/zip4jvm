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
package ru.olegcherednik.zip4jvm.io.zstd;

import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.settings.CompressionLevelEnum;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 07.11.2021
 */
public class ZstdOutputStream extends OutputStream {

    private final com.github.luben.zstd.ZstdOutputStream out;

    public ZstdOutputStream(DataOutput out, CompressionLevelEnum compressionLevel) {
        this.out = Quietly.doRuntime(() -> {
            OutputStream outStream = new Decorator(out);
            int level = compressionLevel(compressionLevel);
            return new com.github.luben.zstd.ZstdOutputStream(outStream, level);
        });
    }

    @Override
    public void write(int val) throws IOException {
        out.write(val);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        out.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    // ---------- static ----------

    private static int compressionLevel(CompressionLevelEnum compressionLevel) {
        if (compressionLevel == CompressionLevelEnum.SUPER_FAST)
            return 1;
        if (compressionLevel == CompressionLevelEnum.FAST)
            return 2;
        if (compressionLevel == CompressionLevelEnum.NORMAL)
            return 3;
        if (compressionLevel == CompressionLevelEnum.MAXIMUM)
            return 17;
        return 3;
    }

    @RequiredArgsConstructor
    private static final class Decorator extends OutputStream {

        private final DataOutput out;

        @Override
        public void write(int val) throws IOException {
            out.writeByte(val);
        }

        @Override
        public void write(byte[] buf, int offs, int len) throws IOException {
            out.write(buf, offs, len);
        }

    }

}
