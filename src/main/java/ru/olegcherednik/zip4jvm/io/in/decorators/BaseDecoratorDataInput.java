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
package ru.olegcherednik.zip4jvm.io.in.decorators;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.utils.ThreadLocalBuffer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * This is a base implementation of decorators for {@link DataInput}.
 *
 * @param <T> {@link DataInput}
 * @author Oleg Cherednik
 * @since 19.11.2024
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseDecoratorDataInput<T extends DataInput> implements DataInput {

    protected final T in;

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
    public int readByte() {
        return in.readByte();
    }

    @Override
    public int readWord() {
        return in.readWord();
    }

    @Override
    public long readDword() {
        return in.readDword();
    }

    @Override
    public long readQword() {
        return in.readQword();
    }

    @Override
    public long skip(long bytes) {
        return in.skip(bytes);
    }

    // ---------- ReadBuffer ----------

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

    // ---------- Marker ----------

    @Override
    public final void mark(String id) {
        in.mark(id);
    }

    @Override
    public final long getMark(String id) {
        return in.getMark(id);
    }

    @Override
    public final long getMarkSize(String id) {
        return in.getMarkSize(id);
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() {
        in.close();
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return in.toString();
    }

}
