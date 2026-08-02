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

import ru.olegcherednik.zip4jvm.io.in.ReadBuffer;
import ru.olegcherednik.zip4jvm.io.out.WriteBuffer;
import ru.olegcherednik.zip4jvm.utils.ByteUtils;

import java.math.BigInteger;

/**
 * @author Oleg Cherednik
 * @since 01.11.2024
 */
public enum ByteOrder {

    LITTLE_ENDIAN {

        // ---------- read ----------

        @Override
        public int readByte(ReadBuffer in) {
            return ByteUtils.readByte(in);
        }

        @Override
        public int readWord(ReadBuffer in) {
            return ByteUtils.readWord(in);
        }

        @Override
        public long readDword(byte[] buf, int offs) {
            return ByteUtils.readDword(buf, offs);
        }

        @Override
        public long readDword(ReadBuffer in) {
            return ByteUtils.readDword(in);
        }

        @Override
        public long readQword(ReadBuffer in) {
            return ByteUtils.readQword(in);
        }

        @Override
        public BigInteger readBigInteger(int size, ReadBuffer in) {
            return ByteUtils.readBigInteger(size, in);
        }

        // ---------- write ----------

        @Override
        public void writeByte(int val, WriteBuffer out) {
            ByteUtils.writeByte(val, out);
        }

        @Override
        public void writeWord(int val, WriteBuffer out) {
            ByteUtils.writeWord(val, out);
        }

        @Override
        public void writeDword(long val, WriteBuffer out) {
            ByteUtils.writeDword(val, out);
        }

        @Override
        public void writeQword(long val, WriteBuffer out) {
            ByteUtils.writeQword(val, out);
        }

    };

    // ---------- read ----------

    public abstract int readByte(ReadBuffer in);

    public abstract int readWord(ReadBuffer in);

    public abstract long readDword(ReadBuffer in);

    public abstract long readDword(byte[] buf, int offs);

    public abstract long readQword(ReadBuffer in);

    public abstract BigInteger readBigInteger(int size, ReadBuffer in);

    // ---------- write ----------

    public abstract void writeByte(int val, WriteBuffer out);

    public abstract void writeWord(int val, WriteBuffer out);

    public abstract void writeDword(long val, WriteBuffer out);

    public abstract void writeQword(long val, WriteBuffer out);

}
