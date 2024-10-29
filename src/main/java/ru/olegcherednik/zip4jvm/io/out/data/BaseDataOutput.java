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

import ru.olegcherednik.zip4jvm.io.AbstractMarker;

import java.io.IOException;

/**
 * This class contains general logic of {@link DataOutput}. Subclasses must
 * implement {@link BaseDataOutput#writeInternal(byte[], int, int)} only. All
 * other methods are not mandatory to override.
 *
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
public abstract class BaseDataOutput extends AbstractMarker implements DataOutput {

    private static final int OFFS_BYTE = 0;
    private static final int OFFS_WORD = 1;
    private static final int OFFS_DWORD = 3;
    private static final int OFFS_QWORD = 7;

    private static final ThreadLocal<byte[]> THREAD_LOCAL_BUF = ThreadLocal.withInitial(() -> new byte[15]);

    @Override
    public void writeByte(int val) throws IOException {
        convertAndWrite(val, OFFS_BYTE, 1);
    }

    @Override
    public void writeWord(int val) throws IOException {
        convertAndWrite(val, OFFS_WORD, 2);
    }

    @Override
    public void writeDword(long val) throws IOException {
        convertAndWrite(val, OFFS_DWORD, 4);
    }

    @Override
    public void writeQword(long val) throws IOException {
        convertAndWrite(val, OFFS_QWORD, 8);
    }

    private void convertAndWrite(long val, int offs, int len) throws IOException {
        byte[] buf = THREAD_LOCAL_BUF.get();
        fromLong(val, buf, offs, len);
        write(buf, offs, len);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        long offsFrom = getRelativeOffs();
        writeInternal(buf, offs, len);
        incTic(getRelativeOffs() - offsFrom);
    }

    protected abstract void writeInternal(byte[] buf, int offs, int len) throws IOException;

}
