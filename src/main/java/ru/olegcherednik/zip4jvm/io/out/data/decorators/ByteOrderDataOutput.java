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
package ru.olegcherednik.zip4jvm.io.out.data.decorators;

import ru.olegcherednik.zip4jvm.io.out.data.ByteOrderConverter;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.11.2024
 */
public class ByteOrderDataOutput extends BaseDataOutput {

    private final ByteOrderConverter byteOrderConverter;

    protected ByteOrderDataOutput(DataOutput delegate) {
        super(delegate);
        byteOrderConverter = new ByteOrderConverter(delegate.getByteOrder());
    }

    // ---------- DataOutput ----------

    @Override
    public void writeByte(int val) throws IOException {
        byteOrderConverter.writeByte(val, this);
    }

    @Override
    public void writeWord(int val) throws IOException {
        byteOrderConverter.writeWord(val, this);
    }

    @Override
    public void writeDword(long val) throws IOException {
        byteOrderConverter.writeDword(val, this);
    }

    @Override
    public void writeQword(long val) throws IOException {
        byteOrderConverter.writeQword(val, this);
    }

}
