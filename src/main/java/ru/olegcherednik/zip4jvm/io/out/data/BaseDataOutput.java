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
package ru.olegcherednik.zip4jvm.io.out.data;

import ru.olegcherednik.zip4jvm.io.BaseMarker;
import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.utils.BitUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * This class contains general logic of {@link DataOutput}. Subclasses must
 * implement {@link BaseDataOutput#write(int)} only. All other methods are not
 * mandatory to override.
 *
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseDataOutput extends DataOutput {

    private static final int OFFS_BYTE = 0;
    private static final int OFFS_WORD = 1;
    private static final int OFFS_DWORD = 3;
    private static final int OFFS_QWORD = 7;

    private static final ThreadLocal<byte[]> THREAD_LOCAL_BUF = ThreadLocal.withInitial(() -> new byte[15]);

    private final BaseMarker marker = new BaseMarker();
    private final ByteOrder byteOrder;

    @Override
    public void writeByte(int val) throws IOException {
        write((byte) val);
    }

    @Override
    public void writeWord(int val) throws IOException {
        val = byteOrder.convertWord(val);

        for (int i = 0; i < 2; i++)
            write(BitUtils.getByte(val, i));
    }

    @Override
    public void writeDword(long val) throws IOException {
        val = byteOrder.convertDword(val);

        for (int i = 0; i < 4; i++)
            write(BitUtils.getByte(val, i));
    }

    @Override
    public void writeQword(long val) throws IOException {
        val = byteOrder.convertQword(val);

        for (int i = 0; i < 8; i++)
            write(BitUtils.getByte(val, i));
    }

    // ---------- OutputStream ----------

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        for (int i = 0; i < len; i++)
            write(buf[offs + i]);
    }

    @Override
    public void write(int b) throws IOException {
        marker.incTic();
    }

    // ---------- Marker ----------

    @Override
    public void mark(String id) {
        marker.mark(id);
    }

    @Override
    public long getMark(String id) {
        return marker.getMark(id);
    }

    @Override
    public long getWrittenBytesAmount(String id) {
        return marker.getWrittenBytesAmount(id);
    }

}
