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
package ru.olegcherednik.zip4jvm.model.builders;

import ru.olegcherednik.zip4jvm.crypto.aes.AesEngine;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.model.AesVersion;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.extrafield.records.AesExtraFieldRecord;

import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 30.08.2019
 */
@RequiredArgsConstructor
final class AesExtraDataRecordBuilder {

    private final ZipEntry zipEntry;

    public AesExtraFieldRecord build() {
        AesStrength strength = AesEngine.getStrength(zipEntry.getEncryptionMethod());

        if (strength == AesStrength.NULL)
            return AesExtraFieldRecord.NULL;

        return AesExtraFieldRecord.builder()
                                  .dataSize(7)
                                  .vendor(AesExtraFieldRecord.VENDOR_AE)
                                  .version(AesVersion.AE_2)
                                  .strength(strength)
                                  .compressionMethod(zipEntry.getCompressionMethod()).build();
    }

}
