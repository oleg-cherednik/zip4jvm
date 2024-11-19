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

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * https://github.com/facebook/zstd/blob/dev/doc/zstd_compression_format.md#frame_header
 *
 * @author Oleg Cherednik
 * @since 06.11.2021
 */
public class ZstdInputStream extends InputStream {

    private final DataInput in;
    private final com.github.luben.zstd.ZstdInputStream zstd;
    private final byte[] buf = new byte[1];
    private final long finalAbsoluteOffs;
    private long bytesToRead;

    public ZstdInputStream(DataInput in, long uncompressedSize, long compressedSize) {
        try {
            this.in = in;
            zstd = new com.github.luben.zstd.ZstdInputStream(new Decorator(in));
            finalAbsoluteOffs = in.getAbsOffs() + compressedSize;
            bytesToRead = uncompressedSize;
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    @Override
    public int read() throws IOException {
        return read(buf, 0, 1) == -1 ? -1 : (buf[0] & 0xFF);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        if (bytesToRead <= 0) {
            // TODO I do not know why we do this
            // in.seek(finalAbsoluteOffs);
            return IOUtils.EOF;
        }

        int total = zstd.read(buf, offs, len);
        bytesToRead -= total;
        return total;
    }

    @RequiredArgsConstructor
    private static final class Decorator extends InputStream {

        private final DataInput in;

        @Override
        public int read() throws IOException {
            return in.readByte();
        }

        @Override
        public int read(byte[] buf, int offs, int len) throws IOException {
            return in.read(buf, offs, len);
        }

        @Override
        public void close() throws IOException {
            if (in instanceof Closeable)
                ((Closeable) in).close();
        }

    }

}


