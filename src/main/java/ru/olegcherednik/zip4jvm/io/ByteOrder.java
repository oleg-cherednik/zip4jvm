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
package ru.olegcherednik.zip4jvm.io;

import ru.olegcherednik.zip4jvm.io.in.data.xxx.DataInput;
import ru.olegcherednik.zip4jvm.utils.BitUtils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 01.11.2024
 */
public enum ByteOrder {

    LITTLE_ENDIAN {

        // ---------- read ----------

        @Override
        public int readByte(DataInput in) throws IOException {
            return BitUtils.readByte(in);
        }

        @Override
        public int readWord(DataInput in) throws IOException {
            return BitUtils.readWord(in);
        }

        @Override
        public long readDword(DataInput in) throws IOException {
            return BitUtils.readDword(in);
        }

        @Override
        public long readQword(DataInput in) throws IOException {
            return BitUtils.readQword(in);
        }

        @Override
        public long getLong(byte[] buf, int offs, int len) {
            long res = 0;

            for (int i = offs + len - 1; i >= offs; i--)
                res = res << 8 | buf[i] & 0xFF;

            return res;
        }

        // ---------- write ----------

        @Override
        public void writeByte(int val, OutputStream out) throws IOException {
            BitUtils.writeByte(val, out);
        }

        @Override
        public void writeWord(int val, OutputStream out) throws IOException {
            BitUtils.writeWord(val, out);
        }

        @Override
        public void writeDword(long val, OutputStream out) throws IOException {
            BitUtils.writeDword(val, out);
        }

        @Override
        public void writeQword(long val, OutputStream out) throws IOException {
            BitUtils.writeQword(val, out);
        }

    };

    // ---------- read ----------

    public abstract int readByte(DataInput in) throws IOException;

    public abstract int readWord(DataInput in) throws IOException;

    public abstract long readDword(DataInput in) throws IOException;

    public abstract long readQword(DataInput in) throws IOException;

    public abstract long getLong(byte[] buf, int offs, int len);

    // ---------- write ----------

    public abstract void writeByte(int val, OutputStream out) throws IOException;

    public abstract void writeWord(int val, OutputStream out) throws IOException;

    public abstract void writeDword(long val, OutputStream out) throws IOException;

    public abstract void writeQword(long val, OutputStream out) throws IOException;

}
