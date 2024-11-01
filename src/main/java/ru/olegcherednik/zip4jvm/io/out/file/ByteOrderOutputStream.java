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
package ru.olegcherednik.zip4jvm.io.out.file;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This is a decorator for {@link OutputStream} that adds ability to define
 * a byte order for the digital number. Subclasses should implement given
 * {@link ByteOrderOutputStream#fromLong(long, byte[], int, int)} method.
 *
 * @author Oleg Cherednik
 * @since 08.08.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ByteOrderOutputStream extends OutputStream {

    private final OutputStream os;
    @Getter
    private long relativeOffs;

    public abstract void fromLong(long val, byte[] buf, int offs, int len);

    @Override
    public void write(int b) throws IOException {
        os.write(b);
        relativeOffs++;
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        os.write(buf, offs, len);
        relativeOffs += len;
    }

    @Override
    public void flush() throws IOException {
        os.flush();
    }

    @Override
    public void close() throws IOException {
        os.close();
    }

    @Override
    public String toString() {
        return "offs: " + getRelativeOffs() + " (0x" + Long.toHexString(getRelativeOffs()) + ')';
    }

}
