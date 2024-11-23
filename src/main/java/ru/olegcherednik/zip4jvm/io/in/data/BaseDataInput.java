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
package ru.olegcherednik.zip4jvm.io.in.data;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.utils.ThreadLocalBuffer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 19.11.2024
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseDataInput implements DataInput {

    protected final DataInput in;

    // ---------- DataInput ----------

    @Override
    public ByteOrder getByteOrder() {
        return in.getByteOrder();
    }

    @Override
    public long getAbsOffs() {
        return in.getAbsOffs();
    }

    @Override
    public int readByte() throws IOException {
        return in.readByte();
    }

    @Override
    public int readWord() throws IOException {
        return in.readWord();
    }

    @Override
    public long readDword() throws IOException {
        return in.readDword();
    }

    @Override
    public long readQword() throws IOException {
        return in.readQword();
    }

    @Override
    public String readNumber(int bytes, int radix) throws IOException {
        return in.readNumber(bytes, radix);
    }

    @Override
    public long skip(long bytes) throws IOException {
        return in.skip(bytes);
    }

    // ---------- ReadBuffer ----------

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return in.read(buf, offs, len);
    }

    @Override
    public final int read() throws IOException {
        byte[] buf = ThreadLocalBuffer.getOne();
        int b = read(buf, 0, buf.length);
        return b == IOUtils.EOF ? IOUtils.EOF : buf[0] & 0xFF;
    }

    // ---------- Marker ----------

    @Override
    public void mark(String id) {
        in.mark(id);
    }

    @Override
    public long getMark(String id) {
        return in.getMark(id);
    }

    @Override
    public long getMarkSize(String id) {
        return in.getMarkSize(id);
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        in.close();
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return in.toString();
    }

}
