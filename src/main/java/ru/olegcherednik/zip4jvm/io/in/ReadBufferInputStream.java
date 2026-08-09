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
package ru.olegcherednik.zip4jvm.io.in;

import ru.olegcherednik.zip4jvm.utils.ThreadLocalBuffer;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 19.11.2024
 */
@RequiredArgsConstructor
public class ReadBufferInputStream extends InputStream {

    private final ReadBuffer in;

    public static ReadBufferInputStream create(ReadBuffer in) {
        return new ReadBufferInputStream(in);
    }

    // ---------- InputStream ----------

    @Override
    public int read(byte[] buf, int offs, int len) {
        return in.read(buf, offs, len);
    }

    @Override
    public final int read() {
        byte[] buf = ThreadLocalBuffer.getOne();
        read(buf, 0, buf.length);
        return buf[0] & 0xFF;
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return in.toString();
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        in.close();
    }
}
