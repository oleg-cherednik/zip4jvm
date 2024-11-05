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
package ru.olegcherednik.zip4jvm.io.out.entry.compressed;

import ru.olegcherednik.zip4jvm.io.lzma.LzmaInputStream;
import ru.olegcherednik.zip4jvm.io.lzma.LzmaOutputStream;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 09.02.2020
 */
final class LzmaEntryDataOutput extends CompressedEntryDataOutput {

    private final LzmaOutputStream lzma;
    private boolean writeHeader = true;

    LzmaEntryDataOutput(DataOutput out, CompressionLevel compressionLevel, boolean eosMarker, long uncompressedSize) {
        super(out);
        lzma = createOutputStream(out, compressionLevel, eosMarker, uncompressedSize);
    }

    private static LzmaOutputStream createOutputStream(DataOutput out,
                                                       CompressionLevel compressionLevel,
                                                       boolean eosMarker,
                                                       long uncompressedSize) {
        return Quietly.doQuietly(() -> {
            LzmaInputStream.Properties properties = new LzmaInputStream.Properties(compressionLevel);
            return new LzmaOutputStream(out, properties, eosMarker ? -1 : uncompressedSize);
        });
    }

    @Override
    public void write(int b) throws IOException {
        if (writeHeader) {
            delegate.writeByte((byte) 19);    // major version
            delegate.writeByte((byte) 0);     // minor version
            delegate.writeWord(5);            // header size
            lzma.writeHeader();
            writeHeader = false;
        }

        lzma.write(b);
    }

    @Override
    public void close() throws IOException {
        lzma.close();
    }

}
