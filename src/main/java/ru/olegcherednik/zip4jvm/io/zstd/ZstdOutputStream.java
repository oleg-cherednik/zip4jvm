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

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
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

    public ZstdOutputStream(DataOutput out, CompressionLevel compressionLevel) {
        this.out = Quietly.doQuietly(() -> {
            OutputStream outStream = new Decorator(out);
            int level = compressionLevel(compressionLevel);
            return new com.github.luben.zstd.ZstdOutputStream(outStream, level);
        });
    }

    private static int compressionLevel(CompressionLevel compressionLevel) {
        if (compressionLevel == CompressionLevel.SUPER_FAST)
            return 1;
        if (compressionLevel == CompressionLevel.FAST)
            return 2;
        if (compressionLevel == CompressionLevel.NORMAL)
            return 3;
        if (compressionLevel == CompressionLevel.MAXIMUM)
            return 17;
        return 3;
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
