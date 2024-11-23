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
package ru.olegcherednik.zip4jvm.io.in.data.compressed;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.lzma.LzmaInputStream;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 02.02.2020
 */
public final class LzmaDataInput extends CompressedEntryDataInput {

    private static final String HEADER = LzmaDataInput.class.getSimpleName() + ".header";
    private static final int HEADER_SIZE = 5;

    private final LzmaInputStream lzma;

    public LzmaDataInput(ZipEntry zipEntry, DataInput in) {
        super(in);
        lzma = createInputStream(zipEntry, in);
    }

    private static LzmaInputStream createInputStream(ZipEntry zipEntry, DataInput in) {
        return Quietly.doQuietly(() -> {
            in.mark(HEADER);
            in.skip(1); // major version
            in.skip(1); // minor version
            int headerSize = in.readWord();

            if (headerSize != HEADER_SIZE)
                throw new Zip4jvmException(String.format("LZMA header size expected %d bytes: actual is %d bytes",
                                                         HEADER_SIZE, headerSize));

            long uncompressedSize = zipEntry.isLzmaEosMarker() ? -1 : zipEntry.getUncompressedSize();
            return new LzmaInputStream(in, uncompressedSize);
        });
    }

    // ---------- ReadBuffer ----------

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int readNow = lzma.read(buf, offs, len);
        return readNow == IOUtils.EOF || readNow == 0 ? IOUtils.EOF : readNow;
    }

}
