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
package ru.olegcherednik.zip4jvm.model.builders;

import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.extrafield.records.AesExtraFieldRecord;

import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 30.08.2019
 */
@RequiredArgsConstructor
final class AesExtraFieldRecordBuilder {

    private final ZipEntry zipEntry;

    AesExtraFieldRecord build() {
        AesStrength strength = AesStrength.of(zipEntry.getEncryption());

        if (strength == AesStrength.NULL)
            return AesExtraFieldRecord.NULL;

        return AesExtraFieldRecord.builder()
                                  .dataSize(7)
                                  .vendor(AesExtraFieldRecord.VENDOR_AE)
                                  .version(zipEntry.getAesVersion())
                                  .strength(strength)
                                  .compressionMethod(zipEntry.getCompression()).build();
    }

}
