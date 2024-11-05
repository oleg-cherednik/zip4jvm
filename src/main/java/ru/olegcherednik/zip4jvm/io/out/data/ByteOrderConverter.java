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

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.utils.BitUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 02.11.2024
 */
@Getter
@RequiredArgsConstructor
public class ByteOrderConverter {

    private final ByteOrder byteOrder;

    public void writeByte(int val, OutputStream out) throws IOException {
        out.write((byte) val);
    }

    public void writeWord(int val, OutputStream out) throws IOException {
        val = byteOrder.convertWord(val);

        for (int i = 0; i < 2; i++)
            out.write(BitUtils.getByte(val, i));
    }

    public void writeDword(long val, OutputStream out) throws IOException {
        val = byteOrder.convertDword(val);

        for (int i = 0; i < 4; i++)
            out.write(BitUtils.getByte(val, i));
    }

    public void writeQword(long val, OutputStream out) throws IOException {
        val = byteOrder.convertQword(val);

        for (int i = 0; i < 8; i++)
            out.write(BitUtils.getByte(val, i));
    }


}
