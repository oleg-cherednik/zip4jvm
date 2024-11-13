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
import ru.olegcherednik.zip4jvm.io.in.file.DataInputFile;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.Getter;

/**
 * @author Oleg Cherednik
 * @since 12.11.2024
 */
@Getter
public abstract class RandomAccessFileBaseDataInput extends BaseDataInput implements DataInputFile {

    protected final SrcZip srcZip;

    protected RandomAccessFileBaseDataInput(SrcZip srcZip) {
        this.srcZip = srcZip;
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
    public int readByte() {
        return Quietly.doQuietly(() -> getByteOrder().readByte(this));
    }

    @Override
    public int readWord() {
        return Quietly.doQuietly(() -> getByteOrder().readWord(this));
    }

    @Override
    public long readDword() {
        return Quietly.doQuietly(() -> getByteOrder().readDword(this));
    }

    @Override
    public long readQword() {
        return Quietly.doQuietly(() -> getByteOrder().readQword(this));
    }

}
