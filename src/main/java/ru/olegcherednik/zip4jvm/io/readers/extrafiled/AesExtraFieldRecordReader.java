/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.AesVersion;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.charset.Charsets;
import ru.olegcherednik.zip4jvm.model.extrafield.records.AesExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor
public final class AesExtraFieldRecordReader implements Reader<AesExtraFieldRecord> {

    private final int size;

    @Override
    public AesExtraFieldRecord read(DataInput in) {
        AesVersion version = AesVersion.parseCode(in.readWord());
        String vendor = in.readString(2, Charsets.UTF_8);
        AesStrength strength = AesStrength.of(in.readByte());
        Compression compression = Compression.parseCode(in.readWord());

        return AesExtraFieldRecord.builder()
                                  .dataSize(size)
                                  .version(version)
                                  .vendor(vendor)
                                  .strength(strength)
                                  .compressionMethod(compression).build();
    }

    @Override
    public String toString() {
        return String.format("AES (0x%04X)", AesExtraFieldRecord.SIGNATURE);
    }

}
