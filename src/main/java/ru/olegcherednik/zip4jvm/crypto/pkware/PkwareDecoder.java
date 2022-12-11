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
package ru.olegcherednik.zip4jvm.crypto.pkware;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotEmpty;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
@RequiredArgsConstructor
public final class PkwareDecoder implements Decoder {

    private final PkwareEngine engine;
    @Getter
    private final long compressedSize;

    public static PkwareDecoder create(ZipEntry zipEntry, DataInput in) throws IOException {
        requireNotEmpty(zipEntry.getPassword(), zipEntry.getFileName() + ".password");

        PkwareEngine engine = new PkwareEngine(zipEntry.getPassword());
        PkwareHeader.read(engine, zipEntry, in);

        long compressedSize = zipEntry.getCompressedSize() - PkwareHeader.SIZE;
        return new PkwareDecoder(engine, compressedSize);
    }

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        assert len > 0;
        return engine.decrypt(buf, offs, len);
    }

    @Override
    public long getDataCompressedSize(long compressedSize) {
        return this.compressedSize;
    }

}
