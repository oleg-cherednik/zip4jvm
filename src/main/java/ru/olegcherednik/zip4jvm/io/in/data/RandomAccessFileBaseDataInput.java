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
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.ThreadLocalBuffer;

import lombok.Getter;

import java.io.IOException;
import java.math.BigInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 12.11.2024
 */
@Getter
public abstract class RandomAccessFileBaseDataInput extends MarkerDataInput {

    protected final SrcZip srcZip;

    protected RandomAccessFileBaseDataInput(SrcZip srcZip) {
        this.srcZip = srcZip;
    }

    // ---------- DataInput ----------

    @Override
    public long getAbsOffs() {
        return getDisk().getAbsOffs() + getDiskRelativeOffs();
    }

    @Override
    public long availableLong() {
        return srcZip.getSize() - getAbsOffs();
    }

    // ---------- DataInputLocation ??

    public abstract SrcZip.Disk getDisk();

    public abstract long getDiskRelativeOffs();

    // ---------- ???

    @Override
    public final int read() throws IOException {
        byte[] buf = ThreadLocalBuffer.getOne();
        read(buf, 0, buf.length);
        return buf[0] & 0xFF;
    }

    // ----------

    @Override
    public String readNumber(int bytes, int radix) throws IOException {
        if (bytes <= 0)
            return null;

        byte[] buf = readBytes(bytes);

        String hexStr = IntStream.rangeClosed(1, bytes)
                                 .map(i -> buf[buf.length - i] & 0xFF)
                                 .mapToObj(Integer::toHexString)
                                 .collect(Collectors.joining());

        return String.valueOf(new BigInteger(hexStr, radix));
    }

    // ---------- DataInputFile ----------

    @Override
    public ByteOrder getByteOrder() {
        return srcZip.getByteOrder();
    }

    @Override
    public long convertToAbsoluteOffs(int diskNo, long relativeOffs) {
        return srcZip.getDiskByNo(diskNo).getAbsOffs() + relativeOffs;
    }

    // ---------- DataInput ----------

    @Override
    public int readByte() throws IOException {
        return getByteOrder().readByte(this);
    }

    @Override
    public int readWord() throws IOException {
        return getByteOrder().readWord(this);
    }

    @Override
    public long readDword() throws IOException {
        return getByteOrder().readDword(this);
    }

    @Override
    public long readQword() throws IOException {
        return getByteOrder().readQword(this);
    }

}
