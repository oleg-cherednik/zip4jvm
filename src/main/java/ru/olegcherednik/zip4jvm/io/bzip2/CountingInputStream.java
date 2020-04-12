/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.io.bzip2;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream that tracks the number of bytes read.
 *
 * @NotThreadSafe
 * @since 1.3
 */
class CountingInputStream {

    protected volatile InputStream in;

    public CountingInputStream(DataInput in) {
        this.in = new InputStream() {
            @Override
            public int read() throws IOException {
                return in.readByte();
            }
        };
    }

    public int read() throws IOException {
        return in.read();
    }

    public int read(final byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(final byte[] b, final int off, final int len) throws IOException {
        return in.read(b, off, len);
    }

    public void close() throws IOException {
        in.close();
    }

}
