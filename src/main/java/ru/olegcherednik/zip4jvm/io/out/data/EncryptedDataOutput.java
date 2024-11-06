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

import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.BitUtils;

import java.io.IOException;

/**
 * This interface describes ability to write an encrypted data items to the
 * given {@link DataOutput} using given {@link Encoder}. I.e. this is a
 * decorator, that can encrypt incoming data and write it to the given
 * {@link DataOutput}.
 *
 * @author Oleg Cherednik
 * @since 11.02.2020
 */
public class EncryptedDataOutput extends BaseDataOutput {

    private final Encoder encoder;
    private boolean writeHeader = true;

    public static EncryptedDataOutput create(ZipEntry zipEntry, DataOutput out) {
        return new EncryptedDataOutput(zipEntry.createEncoder(), out);
    }

    public EncryptedDataOutput(Encoder encoder, DataOutput out) {
        super(out);
        this.encoder = encoder;
    }

    private void writeEncryptionHeader() throws IOException {
        if (writeHeader) {
            encoder.writeEncryptionHeader(delegate);
            writeHeader = false;
        }
    }

    public void encodingAccomplished() throws IOException {
        encoder.close(delegate);
    }

    // ---------- DataOutput ----------

    @Override
    public void writeByte(int val) throws IOException {
        BitUtils.writeByte(val, this);
    }

    @Override
    public void writeWord(int val) throws IOException {
        BitUtils.writeWord(val, this);
    }

    @Override
    public void writeDword(long val) throws IOException {
        BitUtils.writeDword(val, this);
    }

    @Override
    public void writeQword(long val) throws IOException {
        BitUtils.writeQword(val, this);
    }

    // ---------- OutputStream ----------

    @Override
    public void write(int b) throws IOException {
        writeEncryptionHeader();
        b = encoder.encrypt((byte) b);
        super.write(b);
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        writeEncryptionHeader();
        super.close();
    }

}
