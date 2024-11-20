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
import ru.olegcherednik.zip4jvm.io.in.data.MarkerDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.xxx.XxxDataInput;
import ru.olegcherednik.zip4jvm.utils.ThreadLocalBuffer;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

import lombok.Getter;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;

/**
 * {@link XxxDataInput} based on the given byte array
 *
 * @author Oleg Cherednik
 * @since 22.12.2022
 */
public class ByteArrayDataInput extends MarkerDataInput {

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
    public long getAbsOffs() {
        return offs;
    }

    @Override
    public long availableLong() throws IOException {
        return buf.length - offs;
    }

    @Override
    public void seek(long absOffs) {
        if (absOffs >= 0 && absOffs < buf.length)
            offs = (int) absOffs;
    }

    // ---------- ???

    @Override
    public final int read() throws IOException {
        byte[] buf = ThreadLocalBuffer.getOne();
        read(buf, 0, buf.length);
        return buf[0] & 0xFF;
    }

    // ----------

    @Override
    public int readByte() throws IOException {
        return byteOrder.readByte(this);
    }

    @Override
    public int readWord() throws IOException {
        return byteOrder.readWord(this);
    }

    @Override
    public long readDword() throws IOException {
        return byteOrder.readDword(this);
    }

    @Override
    public long readQword() throws IOException {
        return byteOrder.readQword(this);
    }

    // ---------- RandomAccess ----------

    @Override
    public long skip(long bytes) {
        ValidationUtils.requireZeroOrPositive(bytes, "skip.bytes");

        bytes = Math.min(bytes, buf.length - offs);
        offs += bytes;
        return bytes;
    }

    // ---------- InputStream ----------

//    @Override
//    public int read() throws IOException {
//        return 0;
//    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int l = Math.min(len, this.buf.length - this.offs);

        for (int i = 0; i < l; i++)
            buf[offs + i] = this.buf[this.offs++];

        return l;
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return "offs: " + offs + " (0x" + Long.toHexString(offs) + ')';
    }


    // --------- smth from old DataInput

    @Override
    public String readNumber(int bytes, int radix) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public void seek(int diskNo, long relativeOffs) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public void seek(String id) throws IOException {
        seek(getMark(id));
    }

    @Override
    public long convertToAbsoluteOffs(int diskNo, long relativeOffs) {
        throw new NotImplementedException();
    }

}
