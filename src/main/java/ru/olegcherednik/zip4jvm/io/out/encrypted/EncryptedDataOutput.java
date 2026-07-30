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
package ru.olegcherednik.zip4jvm.io.out.encrypted;

import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.io.out.BaseDataOutput;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.ByteUtils;

/**
 * This class describes ability to write an encrypted data items to the given
 * {@link DataOutput} using given {@link Encoder}. I.e. this is a decorator,
 * that can encrypt incoming data and write it to the given delegate
 * {@link DataOutput}.
 *
 * @author Oleg Cherednik
 * @since 11.02.2020
 */
public class EncryptedDataOutput extends BaseDataOutput {

    private final Encoder encoder;
    private boolean writeHeader = true;

    public static EncryptedDataOutput create(ZipEntry zipEntry, DataOutput out) {
        return create(zipEntry.createEncoder(), out);
    }

    public static EncryptedDataOutput create(Encoder encoder, DataOutput out) {
        return new EncryptedDataOutput(encoder, out);
    }

    protected EncryptedDataOutput(Encoder encoder, DataOutput out) {
        super(out);
        this.encoder = encoder;
    }

    private void writeEncryptionHeader() {
        if (writeHeader) {
            encoder.writeEncryptionHeader(out);
            writeHeader = false;
        }
    }

    // ---------- DataOutput ----------

    @Override
    public void writeByte(int val) {
        ByteUtils.writeByte(val, this);
    }

    @Override
    public void writeWord(int val) {
        ByteUtils.writeWord(val, this);
    }

    @Override
    public void writeDword(long val) {
        ByteUtils.writeDword(val, this);
    }

    @Override
    public void writeQword(long val) {
        ByteUtils.writeQword(val, this);
    }

    // ---------- OutputStream ----------

    @Override
    public void write(int b) {
        writeEncryptionHeader();
        encoder.writeEncrypted(b, out);
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() {
        writeEncryptionHeader();
        encoder.close(out);
        super.close();
    }

}
