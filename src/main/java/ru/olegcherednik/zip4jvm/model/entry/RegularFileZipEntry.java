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
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.xxx.XxxDataInput;
import ru.olegcherednik.zip4jvm.model.AesVersion;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.function.ZipEntryInputStreamFunction;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@Getter
@Setter
final class RegularFileZipEntry extends ZipEntry {

    private long checksum;

    RegularFileZipEntry(String fileName,
                        int lastModifiedTime,
                        ExternalFileAttributes externalFileAttributes,
                        AesVersion aesVersion,
                        CompressionMethod compressionMethod,
                        CompressionLevel compressionLevel,
                        EncryptionMethod encryptionMethod,
                        ZipEntryInputStreamFunction inputStreamFunction) {
        super(ZipUtils.getFileName(fileName, false),
              lastModifiedTime,
              externalFileAttributes,
              aesVersion,
              compressionMethod,
              compressionLevel,
              encryptionMethod,
              inputStreamFunction);
    }

    @Override
    public Decoder createDecoder(XxxDataInput in) {
        return encryptionMethod.createDecoder(this, in);
    }

    @Override
    public Encoder createEncoder() {
        return encryptionMethod.createEncoder(this);
    }

}
