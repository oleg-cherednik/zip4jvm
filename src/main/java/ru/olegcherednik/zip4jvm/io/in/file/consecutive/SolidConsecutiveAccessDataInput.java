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
package ru.olegcherednik.zip4jvm.io.in.file.consecutive;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import lombok.Getter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireZeroOrPositive;

/**
 * @author Oleg Cherednik
 * @since 25.11.2024
 */
public class SolidConsecutiveAccessDataInput extends BaseConsecutiveAccessDataInput {

    @Getter
    private final ByteOrder byteOrder;
    private final InputStream in;

    public SolidConsecutiveAccessDataInput(SrcZip srcZip) throws IOException {
        byteOrder = srcZip.getByteOrder();
        in = new BufferedInputStream(Files.newInputStream(srcZip.getDiskByNo(0).getPath()));
    }

    // ---------- DataInput ----------

    @Override
    public long skip(long bytes) throws IOException {
        requireZeroOrPositive(bytes, "skip.bytes");

        long skipped = in.skip(bytes);
        incAbsOffs(skipped);
        return skipped;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int readNow = in.read(buf, offs, len);

        if (readNow > 0)
            incAbsOffs(readNow);

        return readNow;
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        in.close();
        super.close();
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return PathUtils.getOffsStr(getAbsOffs());
    }

}
