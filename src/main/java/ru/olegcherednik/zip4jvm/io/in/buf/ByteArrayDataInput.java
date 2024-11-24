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
import ru.olegcherednik.zip4jvm.io.in.data.BaseRandomAccessDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;

/**
 * {@link DataInput} based on the given byte array
 *
 * @author Oleg Cherednik
 * @since 22.12.2022
 */
@RequiredArgsConstructor
public class ByteArrayDataInput extends BaseRandomAccessDataInput {

    private final byte[] buf;
    @Getter
    private final ByteOrder byteOrder;
    private final int lo;
    private final int len;

    private int available;
    private int offs;

    @SuppressWarnings({ "AssignmentOrReturnOfFieldWithMutableType", "PMD.ArrayIsStoredDirectly" })
    public ByteArrayDataInput(byte[] buf, int offs, int len, ByteOrder byteOrder) {
        this.buf = buf;
        this.byteOrder = byteOrder;
        this.len = len;
        lo = offs;
        available = len;
    }

    // ---------- DataInput ----------

    @Override
    public long getAbsOffs() {
        return offs;
    }

    @Override
    public long availableLong() throws IOException {
        return available;
    }

    @Override
    public long skip(long bytes) throws IOException {
        ValidationUtils.requireZeroOrPositive(bytes, "skip.bytes");
        // TODO check that bytes less than Integer.MAX_VALUE

        int b = (int) Math.min(bytes, available);
        offs += b;
        return bytes;
    }

    // ---------- RandomAccessDataInput ----------

    @Override
    public void seek(long absOffs) throws IOException {
        ValidationUtils.requireZeroOrPositive(absOffs, "seek.absOffs");

        if (absOffs >= 0 && absOffs < len)
            offs = (int) Math.min(absOffs, len);
    }

    // ---------- ReadBuffer ----------

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int l = Math.min(len, available);

        for (int i = 0; i < l; i++, available--, this.offs++)
            buf[offs + i] = this.buf[lo + this.offs];

        return l;
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return "offs: " + offs + " (0x" + Long.toHexString(offs) + ')';
    }


    // --------- smth from old DataInput

    @Override
    public void seek(int diskNo, long relativeOffs) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public long convertToAbsoluteOffs(int diskNo, long relativeOffs) {
        throw new NotImplementedException();
    }

}
