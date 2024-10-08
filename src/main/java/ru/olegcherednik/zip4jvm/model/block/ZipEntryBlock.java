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
package ru.olegcherednik.zip4jvm.model.block;

import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptionHeaderBlock;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author Oleg Cherednik
 * @since 12.12.2019
 */
@Getter
@RequiredArgsConstructor
public class ZipEntryBlock {

    private final String fileName;

    private LocalFileHeader localFileHeader;
    private DataDescriptor dataDescriptor;
    private DecryptionHeader decryptionHeader;

    private LocalFileHeaderBlock localFileHeaderBlock;
    @Setter
    private EncryptionHeaderBlock encryptionHeaderBlock;
    private Block dataDescriptorBlock;

    public void setLocalFileHeader(LocalFileHeader localFileHeader, LocalFileHeaderBlock block) {
        this.localFileHeader = localFileHeader;
        localFileHeaderBlock = block;
    }

    public void setDecryptionHeader(DecryptionHeader decryptionHeader, EncryptionHeaderBlock encryptionHeaderBlock) {
        this.decryptionHeader = decryptionHeader;
        this.encryptionHeaderBlock = encryptionHeaderBlock;
    }

    public void setDataDescriptor(DataDescriptor dataDescriptor, Block block) {
        this.dataDescriptor = dataDescriptor;
        dataDescriptorBlock = block;
    }

    @Override
    public String toString() {
        return fileName;
    }

    @Getter
    @Setter
    public static final class LocalFileHeaderBlock {

        private final Block content = new Block();
        private final ExtraFieldBlock extraFieldBlock = new ExtraFieldBlock();
    }

}
