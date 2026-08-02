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
package ru.olegcherednik.zip4jvm.io.out.decorators.size;

import ru.olegcherednik.zip4jvm.io.out.BaseDataOutput;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.utils.ByteUtils;

import java.util.function.LongConsumer;

/**
 * @author Oleg Cherednik
 * @since 06.11.2024
 */
public class SizeCalcDataOutput extends BaseDataOutput {

    private final LongConsumer saveSize;
    protected long size;

    public SizeCalcDataOutput(LongConsumer saveSize, DataOutput out) {
        super(out);
        this.saveSize = saveSize;
    }

    // ---------- DataOutput ----------

    // TODO #191
    @Override
    public void writeWord(int val) {
        size += ByteUtils.WORD_SIZE;
        super.writeWord(val);
    }

    // TODO #191
    @Override
    public void writeDword(long val) {
        size += ByteUtils.DWORD_SIZE;
        super.writeDword(val);
    }

    // TODO #191
    @Override
    public void writeQword(long val) {
        size += ByteUtils.QWORD_SIZE;
        super.writeQword(val);
    }

    // ---------- WriteBuffer ----------

    @Override
    public void write(int b) {
        size++;
        super.write(b);
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() {
        saveSize.accept(size);
        super.close();
    }

}
