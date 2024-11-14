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
package ru.olegcherednik.zip4jvm.io.in.buf;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.in.data.BaseDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.Getter;

import java.io.IOException;

/**
 * {@link DataInput} based on the given byte array
 *
 * @author Oleg Cherednik
 * @since 22.12.2022
 */
public class ByteArrayDataInput extends BaseDataInput {

    private final byte[] buf;
    @Getter
    private final ByteOrder byteOrder;
    private int offs;

    @SuppressWarnings({ "AssignmentOrReturnOfFieldWithMutableType", "PMD.ArrayIsStoredDirectly" })
    public ByteArrayDataInput(byte[] buf, ByteOrder byteOrder) {
        this.buf = buf;
        this.byteOrder = byteOrder;
    }

    // ---------- DataInput ----------

    @Override
    public int readByte() {
        return Quietly.doQuietly(() -> byteOrder.readByte(this));
    }

    @Override
    public int readWord() {
        return Quietly.doQuietly(() -> byteOrder.readWord(this));
    }

    @Override
    public long readDword() {
        return Quietly.doQuietly(() -> byteOrder.readDword(this));
    }

    @Override
    public long readQword() {
        return Quietly.doQuietly(() -> byteOrder.readQword(this));
    }

    // ---------- RandomAccess ----------

    @Override
    public long skip(long bytes) {
        ValidationUtils.requireZeroOrPositive(bytes, "skip.bytes");

        bytes = Math.min(bytes, buf.length - offs);
        offs += bytes;
        return bytes;
    }

    @Override
    public void seek(long absoluteOffs) {
        if (absoluteOffs >= 0 && absoluteOffs < buf.length)
            offs = (int) absoluteOffs;
    }

    // ---------- DataInputNew ----------

    @Override
    public long getAbsoluteOffs() {
        return offs;
    }

    @Override
    public long size() {
        return buf.length;
    }

    // ---------- ReadBuffer ----------

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int l = Math.min(len, this.buf.length - this.offs);

        for (int i = 0; i < l; i++)
            buf[offs + i] = (byte) read();

        return l;
    }

    @Override
    public int read() throws IOException {
        return buf[offs++];
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return "offs: " + offs + " (0x" + Long.toHexString(offs) + ')';
    }

    @Override
    public void close() throws IOException {

    }
}
