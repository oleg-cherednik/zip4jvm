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

import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.utils.EmptyInputStreamSupplier;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
final class EmptyDirectoryZipEntry extends ZipEntry {

    EmptyDirectoryZipEntry(String dirName,
                           int lastModifiedTime,
                           ExternalFileAttributes externalFileAttributes) {
        super(ZipUtils.getFileName(dirName, true),
              lastModifiedTime,
              externalFileAttributes,
              CompressionMethod.STORE,
              CompressionLevel.NORMAL,
              EncryptionMethod.OFF,
              EmptyInputStreamSupplier.INSTANCE);
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public void setCompressedSize(long compressedSize) {
        /* nothing to set */
    }

}
