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

import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 02.08.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LittleEndianWriteFile implements WriteFile {

    private final OutputStream os;
    private long relativeOffs;

    public static LittleEndianWriteFile create(Path file) {
        return Quietly.doQuietly(() -> {
            Files.createDirectories(file.getParent());
            OutputStream os = new BufferedOutputStream(Files.newOutputStream(file));
            return new LittleEndianWriteFile(os);
        });
    }

    @Override
    public void fromLong(long val, byte[] buf, int offs, int len) {
        for (int i = 0; i < len; i++) {
            buf[offs + i] = (byte) val;
            val >>= 8;
        }
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        os.write(buf, offs, len);
        relativeOffs += len;
    }

    @Override
    public long getRelativeOffs() {
        return relativeOffs;
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
