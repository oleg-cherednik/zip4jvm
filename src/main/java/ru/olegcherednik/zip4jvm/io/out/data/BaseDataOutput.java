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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * This is a base {@link DataOutput} decorator. It's designed to be inherited
 * by another classes to avoid overriding all methods except once that are
 * required for a concrete implementation. It also defines a delegate
 * {@link BaseDataOutput#out}.
 *
 * @author Oleg Cherednik
 * @since 04.11.2024
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseDataOutput extends DataOutput {

    protected final DataOutput out;

    // ---------- DataOutput ----------

    @Override
    public ByteOrder getByteOrder() {
        return out.getByteOrder();
    }

    @Override
    public long getDiskOffs() {
        return out.getDiskOffs();
    }

    @Override
    public void writeByte(int val) throws IOException {
        out.writeByte(val);
    }

    @Override
    public void writeWord(int val) throws IOException {
        out.writeWord(val);
    }

    @Override
    public void writeDword(long val) throws IOException {
        out.writeDword(val);
    }

    @Override
    public void writeQword(long val) throws IOException {
        out.writeQword(val);
    }

    // ---------- OutputStream ----------

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    // ---------- Flushable ----------

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        out.close();
    }

    // ---------- Marker ----------

    @Override
    public void mark(String id) {
        out.mark(id);
    }

    @Override
    public long getMark(String id) {
        return out.getMark(id);
    }

    @Override
    public long getSize(String id) {
        return out.getSize(id);
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return out.toString();
    }

}
