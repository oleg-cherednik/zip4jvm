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

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.ReadBufferInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * https://github.com/facebook/zstd/blob/dev/doc/zstd_compression_format.md#frame_header
 *
 * @author Oleg Cherednik
 * @since 06.11.2021
 */
public class ZstdInputStream extends InputStream {

    private final com.github.luben.zstd.ZstdInputStream zstd;
    private final byte[] buf = new byte[1];

    public ZstdInputStream(DataInput in) throws IOException {
        zstd = new com.github.luben.zstd.ZstdInputStream(new ReadBufferInputStream(in));
    }

    @Override
    public int read() throws IOException {
        return read(buf, 0, 1) == -1 ? -1 : (buf[0] & 0xFF);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return zstd.read(buf, offs, len);
    }

}


