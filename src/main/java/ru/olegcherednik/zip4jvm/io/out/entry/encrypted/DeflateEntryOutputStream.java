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
package ru.olegcherednik.zip4jvm.io.out.entry.encrypted;

import ru.olegcherednik.zip4jvm.io.out.data.EncoderDataOutput;
import ru.olegcherednik.zip4jvm.io.out.entry.EntryMetadataOutputStream;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;

import java.io.IOException;
import java.util.zip.Deflater;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
final class DeflateEntryOutputStream extends EncryptedEntryOutputStream {

    private static final int FOUR = 4;

    private final EncoderDataOutput encoderDataOutput;
    private final byte[] buf = new byte[1024 * 4];
    private final Deflater deflater = new Deflater();

    public boolean firstBytesRead;

    DeflateEntryOutputStream(CompressionLevel compressionLevel,
                             EncoderDataOutput encoderDataOutput,
                             EntryMetadataOutputStream emos) {
        super(encoderDataOutput, emos);
        this.encoderDataOutput = encoderDataOutput;
        deflater.setLevel(compressionLevel.getCode());
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        super.write(buf, offs, len);
        deflater.setInput(buf, offs, len);

        while (!deflater.needsInput()) {
            deflate();
        }
    }

    private void deflate() throws IOException {
        int len = deflater.deflate(buf, 0, buf.length);

        if (len <= 0)
            return;

        if (deflater.finished()) {
            if (len == FOUR)
                return;
            if (len < FOUR)
                return;
            len -= FOUR;
        }

        if (firstBytesRead)
            encoderDataOutput.write(buf, 0, len);
        else {
            encoderDataOutput.write(buf, 2, len - 2);
            firstBytesRead = true;
        }
    }

    private void finish() throws IOException {
        if (deflater.finished())
            return;

        deflater.finish();

        while (!deflater.finished()) {
            deflate();
        }
    }

    @Override
    public void close() throws IOException {
        finish();
        encoderDataOutput.encodingAccomplished();
        super.close();
    }
}
