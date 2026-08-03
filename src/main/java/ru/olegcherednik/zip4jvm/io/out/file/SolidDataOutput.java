/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
package ru.olegcherednik.zip4jvm.io.out.file;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.out.MarkerDataOutput;
import ru.olegcherednik.zip4jvm.io.out.OffsOutputStream;

import lombok.Getter;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 01.11.2024
 */
public class SolidDataOutput extends MarkerDataOutput {

    @Getter
    protected final ByteOrder byteOrder;
    protected final OffsOutputStream out;

    public SolidDataOutput(ByteOrder byteOrder, Path file) {
        this.byteOrder = byteOrder;
        out = OffsOutputStream.create(file);
    }

    // ---------- DataOutput ----------

    @Override
    public void writeByte(int val) {
        byteOrder.writeByte(val, this);
    }

    @Override
    public void writeWord(int val) {
        byteOrder.writeWord(val, this);
    }

    @Override
    public void writeDword(long val) {
        byteOrder.writeDword(val, this);
    }

    @Override
    public void writeQword(long val) {
        byteOrder.writeQword(val, this);
    }

    @Override
    public long getDiskOffs() {
        return out.getOffs();
    }

    // ---------- Flushable ----------

    @Override
    public void flush() {
        out.flush();
    }

    // ---------- WriteBuffer ----------

    @Override
    public void write(int b) {
        out.write(b);
        super.write(b);
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() {
        out.close();
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return out.toString();
    }

}
