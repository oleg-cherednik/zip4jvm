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
import ru.olegcherednik.zip4jvm.io.out.OffsOutputStream;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 01.11.2024
 */
public class SolidDataOutput extends MarkerDataOutput {

    @Getter
    protected final ByteOrder byteOrder;
    protected final OffsOutputStream out;

    public SolidDataOutput(ByteOrder byteOrder, Path file) throws IOException {
        this.byteOrder = byteOrder;
        out = OffsOutputStream.create(file);
    }

    // ---------- DataOutput ----------

    @Override
    public void writeByte(int val) throws IOException {
        byteOrder.writeByte(val, this);
    }

    @Override
    public void writeWord(int val) throws IOException {
        byteOrder.writeWord(val, this);
    }

    @Override
    public void writeDword(long val) throws IOException {
        byteOrder.writeDword(val, this);
    }

    @Override
    public void writeQword(long val) throws IOException {
        byteOrder.writeQword(val, this);
    }

    @Override
    public long getDiskOffs() {
        return out.getOffs();
    }

    // ---------- Flushable ----------

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    // ---------- OutputStream ----------

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        super.write(b);
    }

    // ---------- Closeable ----------

    @Override
    public void close() throws IOException {
        out.close();
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return out.toString();
    }

}
