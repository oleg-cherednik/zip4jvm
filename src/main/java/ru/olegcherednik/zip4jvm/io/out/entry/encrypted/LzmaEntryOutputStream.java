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

import ru.olegcherednik.zip4jvm.io.lzma.LzmaInputStream;
import ru.olegcherednik.zip4jvm.io.lzma.LzmaOutputStream;
import ru.olegcherednik.zip4jvm.io.out.data.EncoderDataOutput;
import ru.olegcherednik.zip4jvm.io.out.entry.EntryMetadataOutputStream;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 09.02.2020
 */
final class LzmaEntryOutputStream extends EncryptedEntryOutputStream {

    private final EncoderDataOutput encoderDataOutput;
    private final LzmaOutputStream lzma;
    private boolean writeHeader = true;

    LzmaEntryOutputStream(CompressionLevel compressionLevel,
                          boolean eosMarker,
                          long uncompressedSize,
                          EncoderDataOutput encoderDataOutput,
                          EntryMetadataOutputStream emos) throws IOException {
        super(emos);
        this.encoderDataOutput = encoderDataOutput;
        lzma = createOutputStream(compressionLevel, eosMarker, uncompressedSize, encoderDataOutput);
    }

    private static LzmaOutputStream createOutputStream(CompressionLevel compressionLevel,
                                                       boolean eosMarker,
                                                       long uncompressedSize,
                                                       EncoderDataOutput encoderDataOutput) throws IOException {
        LzmaInputStream.Properties properties = new LzmaInputStream.Properties(compressionLevel);
        return new LzmaOutputStream(encoderDataOutput, properties, eosMarker ? -1 : uncompressedSize);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        super.write(buf, offs, len);

        if (writeHeader) {
            encoderDataOutput.writeByte((byte) 19);    // major version
            encoderDataOutput.writeByte((byte) 0);     // minor version
            encoderDataOutput.writeWord(5);            // header size
            lzma.writeHeader();
            writeHeader = false;
        }

        lzma.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        lzma.close();
        encoderDataOutput.encodingAccomplished();
        super.close();
    }

    @Override
    public String toString() {
        return encoderDataOutput.toString();
    }

}
