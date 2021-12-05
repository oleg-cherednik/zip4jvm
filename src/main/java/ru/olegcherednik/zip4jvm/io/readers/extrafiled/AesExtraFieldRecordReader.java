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
package ru.olegcherednik.zip4jvm.io.readers.extrafiled;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.extrafield.AesExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor
public final class AesExtraFieldRecordReader implements Reader<AesExtraFieldRecord> {

    private final int size;

    @Override
    public AesExtraFieldRecord read(DataInput in) throws IOException {
        int versionNumber = in.readWord();
        String vendor = in.readString(2, Charsets.UTF_8);
        AesStrength strength = AesStrength.parseValue(in.readByte());
        CompressionMethod compressionMethod = CompressionMethod.parseCode(in.readWord());

        return AesExtraFieldRecord.builder()
                                  .dataSize(size)
                                  .versionNumber(versionNumber)
                                  .vendor(vendor)
                                  .strength(strength)
                                  .compressionMethod(compressionMethod).build();
    }

    @Override
    public String toString() {
        return String.format("AES (0x%04X)", AesExtraFieldRecord.SIGNATURE);
    }

}
