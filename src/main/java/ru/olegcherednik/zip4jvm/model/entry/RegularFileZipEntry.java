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
package ru.olegcherednik.zip4jvm.model.entry;

import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.AesVersion;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.settings.CompressionLevelEnum;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import lombok.Setter;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@Setter
final class RegularFileZipEntry extends ZipEntry {

    private long crc32;

    RegularFileZipEntry(String fileName,
                        int lastModifiedTime,
                        ExternalFileAttributes externalFileAttributes,
                        AesVersion aesVersion,
                        Compression compression,
                        CompressionLevelEnum compressionLevel,
                        Encryption encryption) {
        super(ZipUtils.getFileName(fileName, false),
              lastModifiedTime,
              externalFileAttributes,
              aesVersion,
              compression,
              compressionLevel,
              encryption);
    }

    // ---------- ZipEntry ----------

    @Override
    public boolean isRegularFile() {
        return true;
    }

    @Override
    public Decoder createDecoder(DataInput in) {
        return encryption.createDecoder(this, in);
    }

    @Override
    public Encoder createEncoder() {
        return encryption.createEncoder(this);
    }

    @Override
    public long getCrc32() {
        return encryption.isAes() && aesVersion == AesVersion.AE_2 ? 0 : crc32;
    }

}
